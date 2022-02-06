package me.hama.database.hibernate

import me.hama.config.NoArg
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@NoArg
@Entity
@Table(name = "CUSTOMER")
class Customer(
    @Id
    var id: Long,
    @Column(name = "firstName")
    var firstName: String,
    @Column(name = "middleInitial")
    var middleInitial: String,
    @Column(name = "lastName")
    var lastName: String,
    var address: String,
    var city: String,
    var state: String,
    var zipCode: String
) {
    override fun toString(): String {
        return "Customer(id=$id, firstName='$firstName', middleInitial='$middleInitial', lastName='$lastName', address='$address', city='$city', state='$state', zipCode='$zipCode')"
    }
}
