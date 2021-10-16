package me.hama

import org.springframework.hateoas.*
import org.springframework.hateoas.mediatype.alps.Alps
import org.springframework.hateoas.mediatype.alps.Alps.alps
import org.springframework.hateoas.mediatype.alps.Alps.descriptor
import org.springframework.hateoas.mediatype.alps.Type
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.linkTo
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder.methodOn
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*
import java.util.stream.Collectors


@RestController
class HypermediaItemController(
    private val repository: ItemRepository
) {

    @GetMapping("/hypermedia/items/{id}")
    fun findOne(@PathVariable id: String): Mono<EntityModel<Item>> {
        val controller = methodOn(HypermediaItemController::class.java)
        val selfLink = linkTo(controller.findOne(id))
            .withSelfRel()
            .andAffordance(controller.updateItem(Mono.empty(), id))
            .toMono()
        val aggregateLink = linkTo(controller.findAll()).withRel(IanaLinkRelations.ITEM).toMono()

        return Mono.zip(repository.findById(id), selfLink, aggregateLink)
            .map { EntityModel.of(it.t1, Links.of(it.t2, it.t3)) }
    }

    @GetMapping("/hypermedia/items")
    fun findAll(): Mono<CollectionModel<EntityModel<Item>>> {
        val controller = methodOn(HypermediaItemController::class.java)
        val aggregateRoot = linkTo(controller.findAll())
            .withSelfRel()
            .andAffordance(controller.addNewItem(Mono.empty()))
            .toMono()

        return repository.findAll()
            .flatMap { findOne(it.id!!) }
            .collectList()
            .flatMap { models ->
                aggregateRoot.map { selfLink ->
                    CollectionModel.of(models, selfLink)
                }
            }
    }

    @PostMapping("/hypermedia/items")
    fun addNewItem(@RequestBody item: Mono<EntityModel<Item>>): Mono<ResponseEntity<Any>> {
        return item
            .map(EntityModel<Item>::getContent)
            .flatMap { repository.save(it!!) }
            .map(Item::id)
            .flatMap { findOne(it!!) }
            .map {
                ResponseEntity.created(it.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(it.content)
            }
    }

    @PutMapping("/hypermedia/items/{id}")
    fun updateItem(@RequestBody item: Mono<EntityModel<Item>>, @PathVariable id: String): Mono<ResponseEntity<Any>> {
        return item
            .map(EntityModel<Item>::getContent)
            .map { Item(id, it?.name, it?.description, it?.price!!) }
            .flatMap(repository::save)
            .then(findOne(id))
            .map { model ->
                ResponseEntity.noContent().location(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).build()
            }
    }

    @GetMapping(value = ["/hypermedia/items/profile"], produces = [MediaTypes.ALPS_JSON_VALUE])
    fun profile(): Alps {
        return alps()
            .descriptor(
                listOf(
                    descriptor()
                        .id(Item::class.java.simpleName + "-repr")
                        .descriptor(
                            Arrays.stream(Item::class.java.declaredFields)
                                .map { field ->
                                    descriptor()
                                        .name(field.name)
                                        .type(Type.SEMANTIC)
                                        .build()
                                }
                                .collect(Collectors.toList())
                        )
                        .build()
                )
            )
            .build()
    }
}
