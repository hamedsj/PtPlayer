package me.pitok.mapper

interface Mapper<object1,object2> {

    fun mapFirstToSecond(first: object1): object2
    fun mapSecondToFirst(second: object2): object1? {
        return null
    }

}