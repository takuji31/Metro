package jp.takuji31.metro

import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep
import com.google.common.collect.SetMultimap
import com.google.common.collect.Sets
import com.squareup.kotlinpoet.*
import jp.takuji31.metro.annotations.HttpClient
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class CreateRequestBuilderStep(env: ProcessingEnvironment) : ProcessingStep {

    private val filer: Filer = env.filer
    private val elementUtils: Elements = env.elementUtils
    private val messager: Messager = env.messager
    private val typeUtils: Types = env.typeUtils

    override fun annotations(): Set<Class<out Annotation>> {
        return Sets.newHashSet(HttpClient::class.java)
    }

    override fun process(elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>): Set<Element> {
        val elements = elementsByAnnotation.get(HttpClient::class.java)
        for (klass in elements) {
            if (klass.kind.isClass) {
                // TODO: 2017/04/20 error
                throw IllegalStateException(klass.javaClass.name + "is not interface")
            }
            klass
                    .enclosedElements
                    .filter { element -> element.kind == ElementKind.METHOD }
                    .map { o -> o as ExecutableElement }
                    .forEach { element ->
                        val parameters = element.parameters.map { it as VariableElement }

                        if (parameters.isEmpty()) {
                            return@forEach
                        }

                        val packageName = elementUtils.getPackageOf(klass).toString()

                        val methodName = element.simpleName.toString()
                        val reqClassSimpleName = StringUtils.capitalize(methodName) + "Request"
                        val reqClassName = ClassName(packageName, reqClassSimpleName)
                        val builderClassName = ClassName(packageName, reqClassSimpleName, "Builder")

                        val reqClass = TypeSpec
                                .classBuilder(reqClassSimpleName)
                                .addModifiers(KModifier.PUBLIC)
                        val builderClass = TypeSpec
                                .classBuilder("Builder")
                                .addModifiers(KModifier.PUBLIC)
                                .addFun(FunSpec
                                        .constructorBuilder()
                                        .addModifiers(KModifier.PRIVATE)
                                        .build()
                                )
                        val propertySpecs = mutableListOf<PropertySpec>()
                        parameters
                                .forEach { parameter ->
                                    val propertySpec = PropertySpec
                                            .builder(parameter.simpleName.toString(), parameter.asType().asTypeName(),  KModifier.PUBLIC)
                                            .setter(FunSpec.setterBuilder()
                                                    .addParameter(ParameterSpec.get(parameter))
                                                    .addModifiers(KModifier.PRIVATE).build())
                                            .getter(FunSpec.getterBuilder().build())
                                            .initializer(parameter.simpleName.toString())
                                            .build()

                                    reqClass.addProperty(propertySpec)
                                    builderClass.addProperty(propertySpec)
                                    propertySpecs.add(propertySpec)
                                }

                        val constructorParameters = propertySpecs
                                .map({ fieldSpec -> ParameterSpec.builder(fieldSpec.name, fieldSpec.type).build() })

                        val constructor = FunSpec
                                .constructorBuilder()
                                .addModifiers(KModifier.PRIVATE)
                                .addParameters(constructorParameters)
                        reqClass.primaryConstructor(constructor.build())

//                        val builderCode = CodeBlock.builder()
//                                .add("return new \$T(", reqClassName)
//                        propertySpecs.forEach { fieldSpec ->
//                            if (propertySpecs.indexOf(fieldSpec) != 0) {
//                                builderCode.add(" ,")
//                            }
//                            builderCode.add("\$N", fieldSpec)
//                        }
//                        builderCode.add(");\n")
//                        builderClass.addMethod(MethodSpec
//                                .methodBuilder("build")
//                                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
//                                .addCode(builderCode.build())
//                                .returns(reqClassName)
//                                .build()
//                        )

                        propertySpecs.forEach { fieldSpec ->
                            builderClass.addFun(FunSpec
                                    .builder(fieldSpec.name)
                                    .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
                                    .addParameter(ParameterSpec.builder(fieldSpec.name, fieldSpec.type).build())
                                    .returns(builderClassName)
                                    .addStatement("this.\$N = \$N", fieldSpec, fieldSpec)
                                    .addStatement("return this")
                                    .build()
                            )
                        }

                        val serviceParameter = ParameterSpec.builder("service", klass.asType().asTypeName()).build()
                        val requestCode = CodeBlock
                                .builder()
                                .add("return \$N." + element.simpleName + "(", serviceParameter)

                        propertySpecs.forEach { propertySpec ->
                            if (propertySpecs.indexOf(propertySpec) != 0) {
                                requestCode.add(" ,")
                            }
                            requestCode.add("\$N", propertySpec)
                        }

                        requestCode.add(");\n")

                        reqClass.addFun(FunSpec
                                .builder("request")
                                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
                                .returns(element.returnType.asTypeName())
                                .addParameter(serviceParameter)
                                .addCode(requestCode.build())
                                .build()
                        )

                        val builderClassSpec = builderClass.build()
                        reqClass.addType(builderClassSpec)
                        reqClass.addFun(FunSpec
                                .builder("builder")
                                .addModifiers(KModifier.PUBLIC, KModifier.FINAL)
                                .returns(builderClassName)
                                .addStatement("return new \$N()", builderClassSpec)
                                .build()
                        )

                        try {
                            KotlinFile
                                    .builder(packageName, reqClass.build().name!!)
                                    .addType(reqClass.build())
                                    .build()
                                    .writeTo(System.out)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
        }
        return Sets.newHashSet()
    }
}
