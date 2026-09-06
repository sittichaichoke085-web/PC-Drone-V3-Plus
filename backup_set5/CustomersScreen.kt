package com.pcdrone.v3plus.ui.customers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

data class CustomerUi(
    val name: String,
    val phone: String,
    val area: String
)

@Composable
fun CustomersScreen() {

    val customers = remember {
        mutableStateListOf(
            CustomerUi(
                name = "ลูกค้าตัวอย่าง",
                phone = "08X-XXX-XXXX",
                area = "ทองผาภูมิ"
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text("ลูกค้า")
        Text("จัดการรายชื่อลูกค้า")

        customers.forEach { customer ->
            Text("ชื่อ: ${customer.name}")
            Text("โทร: ${customer.phone}")
            Text("พื้นที่: ${customer.area}")
        }

        Button(
            onClick = {
                customers.add(
                    CustomerUi(
                        name = "ลูกค้าใหม่",
                        phone = "-",
                        area = "-"
                    )
                )
            }
        ) {
            Text("เพิ่มลูกค้า")
        }
    }
}
