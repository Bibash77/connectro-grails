package connectro.rest

class Test {

    String firstname;
    Integer age;
    static constraints = {
        firstname size:5..10 ,blank : false
        age: min:20
    }
}