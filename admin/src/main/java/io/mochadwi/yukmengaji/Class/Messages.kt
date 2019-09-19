package io.mochadwi.yukmengaji.Class

class Messages {

    var date: String
    var time: String
    var type: String
    var message: String
    var from: String

    constructor() {

    }

    constructor(date: String, time: String, type: String, message: String, from: String) {
        this.date = date
        this.time = time
        this.type = type
        this.message = message
        this.from = from
    }
}
