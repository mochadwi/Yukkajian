package io.mochadwi.yukmengaji.Class

class Posts {

    var uid: String
    var time: String
    var date: String
    var postimage: String
    var description: String
    var profileimage: String
    var fullname: String

    constructor() {


    }

    constructor(
        uid: String, time: String, date: String, postimage: String, description: String,
        profileimage: String, fullname: String
    ) {
        this.uid = uid
        this.time = time
        this.date = date
        this.postimage = postimage
        this.description = description
        this.profileimage = profileimage
        this.fullname = fullname
    }
}
