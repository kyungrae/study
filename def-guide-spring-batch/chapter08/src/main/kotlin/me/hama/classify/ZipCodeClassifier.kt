package me.hama.classify

import me.hama.Customer
import org.springframework.classify.Classifier

class ZipCodeClassifier<I, O>(
    private val oddItemProcessor: O,
    private val evenItemProcessor: O
) : Classifier<I, O> {
    override fun classify(classifiable: I): O {
        return if (classifiable is Customer && classifiable.zip.toInt() % 2 == 0) evenItemProcessor
        else oddItemProcessor
    }
}
