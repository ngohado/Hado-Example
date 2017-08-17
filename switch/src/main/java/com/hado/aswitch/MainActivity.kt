package com.hado.aswitch

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val data1 = Data("Doan", true, Data2("data 2 name"))
        val data2 = Data()
        data2.name = data1.name
        data2.confirm = data1.confirm
        data2.data2 = data1.data2

        println("Name2: ${data2.name} Name1: ${data1.name}")

        data2.name = "Doan 2"
        data2.data2.name = "Thay doi roi"

        println("Name2: ${data2.name} Name1: ${data1.data2.name}")
    }
}

class Data {
    constructor()
    constructor(name: String, confirm: Boolean) {
        this.name = name
        this.confirm = confirm
    }

    constructor(name: String, confirm: Boolean, data2: Data2) {
        this.name = name
        this.confirm = confirm
        this.data2 = data2
    }


    lateinit var name: String
    var confirm: Boolean = false
    lateinit var data2: Data2
}

class Data2 {
    constructor(name: String) {
        this.name = name
    }

    lateinit var name: String
}


