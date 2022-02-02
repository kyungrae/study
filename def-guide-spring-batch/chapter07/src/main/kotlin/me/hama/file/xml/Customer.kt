package me.hama.file.xml

import me.hama.config.NoArg
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement

@NoArg
@XmlRootElement
class Customer(
    var firstName: String,
    var middleInitial: String,
    var lastName: String,
    var address: String,
    var city: String,
    var state: String,
    var zipCode: String,
    @set:XmlElementWrapper(name = "transactions")
    @set:XmlElement(name = "transaction")
    var transactions: List<Transaction>
)
