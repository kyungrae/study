package me.hama.database.jdbc

class Customer(
    val id: Long,
    val firstName: String,
    val middleInitial: String,
    val lastName: String,
    val address: String,
    val city: String,
    val state: String,
    val zipCode: String
) {
    override fun toString(): String {
        return "Customer(id=$id, firstName='$firstName', middleInitial='$middleInitial', lastName='$lastName', address='$address', city='$city', state='$state', zipCode='$zipCode')"
    }
}
