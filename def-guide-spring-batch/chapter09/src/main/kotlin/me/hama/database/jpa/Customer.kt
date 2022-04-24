package me.hama.database.jpa

import javax.persistence.*

@Entity
@Table(name = "CUSTOMER")
class Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,
    @Column(name = "first_name")
    var firstName: String = "",
    @Column(name = "middle_initial")
    var middleInitial: String = "",
    @Column(name = "last_name")
    var lastName: String = "",
    var address: String = "",
    var city: String = "",
    var state: String = "",
    var zip: String = "",
)
