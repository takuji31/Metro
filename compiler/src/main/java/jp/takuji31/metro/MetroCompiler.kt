package jp.takuji31.metro

import com.google.auto.common.BasicAnnotationProcessor
import com.google.auto.service.AutoService
import javax.annotation.processing.Processor
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class MetroCompiler : BasicAnnotationProcessor() {
    override fun initSteps(): Iterable<BasicAnnotationProcessor.ProcessingStep> {
        return mutableListOf(CreateRequestBuilderStep(processingEnv))
    }
}
