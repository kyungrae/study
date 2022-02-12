package me.hama.database

import org.springframework.batch.core.step.skip.SkipPolicy
import java.io.FileNotFoundException
import java.text.ParseException

class FileVerificationSkipper : SkipPolicy {
    override fun shouldSkip(t: Throwable, skipCount: Int): Boolean {
        return if (t is FileNotFoundException)
            false
        else t is ParseException && skipCount <= 10
    }
}
