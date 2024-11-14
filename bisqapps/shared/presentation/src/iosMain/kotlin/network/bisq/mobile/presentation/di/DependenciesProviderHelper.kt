package network.bisq.mobile.presentation.di

import kotlinx.cinterop.*
import network.bisq.mobile.domain.di.domainModule
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.Qualifier

class DependenciesProviderHelper {

    fun initKoin() {
        val instance = startKoin {
            modules(listOf(domainModule, presentationModule))
        }

        koin = instance.koin
    }

    companion object {
        lateinit var koin: Koin
    }

}

@OptIn(BetaInteropApi::class)
fun Koin.get(objCClass: ObjCClass): Any {
    val kClazz = getOriginalKotlinClass(objCClass)!!
    return get(kClazz, null, null)
}

@OptIn(BetaInteropApi::class)
fun Koin.get(objCClass: ObjCClass, qualifier: Qualifier?, parameter: Any): Any {
    val kClazz = getOriginalKotlinClass(objCClass)!!
    return get(kClazz, qualifier) { parametersOf(parameter) }
}
