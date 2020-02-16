package com.wrongwrong.mapk.core

import com.wrongwrong.mapk.annotations.KConverter
import com.wrongwrong.mapk.annotations.KPropertyAlias
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.staticFunctions
import kotlin.reflect.jvm.isAccessible

internal class ParameterForMap<T : Any> private constructor(
    val param: KParameter,
    val clazz: KClass<T>,
    propertyNameConverter: (String) -> String
) {
    val name: String = param.annotations
        .find { it is KPropertyAlias }
        ?.let { (it as KPropertyAlias).value }
        ?: propertyNameConverter(param.name!!)

    val javaClazz: Class<T> by lazy {
        clazz.java
    }
    // リストの長さが小さいと期待されるためこの形で実装しているが、理想的にはmap的なものが使いたい
    private val creators: Set<Pair<KClass<*>, KFunction<T>>> by lazy {
        creatorsFromConstructors(clazz) + creatorsFromStaticMethods(clazz) + creatorsFromCompanionObject(clazz)
    }

    // 引数の型がcreatorに対して入力可能ならcreatorを返す
    fun <R : Any> getCreator(input: KClass<out R>): KFunction<T>? =
        creators.find { (key, _) -> input.isSubclassOf(key) }?.second

    companion object {
        fun newInstance(param: KParameter, propertyNameConverter: (String) -> String): ParameterForMap<*> {
            return ParameterForMap(param, param.type.classifier as KClass<*>, propertyNameConverter)
        }
    }
}

private fun <T> Collection<KFunction<T>>.getConverterMapFromFunctions(): Set<Pair<KClass<*>, KFunction<T>>> {
    return filter { it.annotations.any { annotation -> annotation is KConverter } }
        .map { func ->
            func.isAccessible = true

            (func.parameters.single().type.classifier as KClass<*>) to func
        }.toSet()
}

private fun <T : Any> creatorsFromConstructors(clazz: KClass<T>): Set<Pair<KClass<*>, KFunction<T>>> {
    return clazz.constructors.getConverterMapFromFunctions()
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> creatorsFromStaticMethods(clazz: KClass<T>): Set<Pair<KClass<*>, KFunction<T>>> {
    val staticFunctions: Collection<KFunction<T>> = clazz.staticFunctions as Collection<KFunction<T>>

    return staticFunctions.getConverterMapFromFunctions()
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> creatorsFromCompanionObject(clazz: KClass<T>): Set<Pair<KClass<*>, KFunction<T>>> {
    return clazz.companionObjectInstance?.let { companionObject ->
        companionObject::class.functions
            .filter { it.annotations.any { annotation -> annotation is KConverter } }
            .map { function ->
                // isAccessibleの書き換えはKotlinの都合で先に行う必要が有る
                function.isAccessible = true
                val func: KFunction<T> = CompanionKFunction(function, companionObject) as KFunction<T>

                (func.parameters.single().type.classifier as KClass<*>) to func
            }.toSet()
    } ?: emptySet()
}
