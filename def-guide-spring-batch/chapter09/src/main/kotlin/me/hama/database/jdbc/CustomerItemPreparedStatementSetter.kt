package me.hama.database.jdbc

import me.hama.database.Customer
import org.springframework.batch.item.database.ItemPreparedStatementSetter
import java.sql.PreparedStatement

class CustomerItemPreparedStatementSetter: ItemPreparedStatementSetter<Customer> {
    override fun setValues(item: Customer, ps: PreparedStatement) {
        ps.setString(1, item.firstName)
        ps.setString(2, item.middleInitial)
        ps.setString(3, item.lastName)
        ps.setString(4, item.address)
        ps.setString(5, item.city)
        ps.setString(6, item.state)
        ps.setString(7, item.zip)
    }
}
