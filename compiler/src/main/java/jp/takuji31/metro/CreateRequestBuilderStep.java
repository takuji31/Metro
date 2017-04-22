package jp.takuji31.metro;

import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import jp.takuji31.metro.annotations.HttpClient;

public class CreateRequestBuilderStep implements ProcessingStep {

    private final Filer filer;
    private final Elements elementUtils;
    private final Messager messager;
    private final Types typeUtils;

    public CreateRequestBuilderStep(ProcessingEnvironment env) {
        this.filer = env.getFiler();
        elementUtils = env.getElementUtils();
        messager = env.getMessager();
        typeUtils = env.getTypeUtils();
    }

    @Override
    public Set<? extends Class<? extends Annotation>> annotations() {
        return Sets.newHashSet(HttpClient.class);
    }

    @Override
    public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
        Set<Element> elements = elementsByAnnotation.get(HttpClient.class);
        for (Element klass : elements) {
            if (klass.getKind().isClass()) {
                // TODO: 2017/04/20 error
                throw new IllegalStateException(klass.getClass().getName() + "is not interface");
            }
            TypeName clientClassName = ClassName.get(klass.asType());
            klass
                .getEnclosedElements()
                .stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .map(o -> ((ExecutableElement) o))
                .forEach(element -> {
                    List<? extends VariableElement> parameters = element.getParameters();

                    if (parameters.isEmpty()) {
                        return;
                    }

                    String packageName = elementUtils.getPackageOf(klass).toString();

                    String methodName = element.getSimpleName().toString();
                    String reqClassSimpleName = StringUtils.capitalize(methodName) + "Request";
                    ClassName reqClassName = ClassName.get(packageName, reqClassSimpleName);
                    ClassName builderClassName = ClassName.get(packageName, reqClassSimpleName, "Builder");

                    TypeSpec.Builder reqClass = TypeSpec
                        .classBuilder(reqClassSimpleName)
                        .addModifiers(Modifier.PUBLIC);
                    TypeSpec.Builder builderClass = TypeSpec
                        .classBuilder("Builder")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addMethod(MethodSpec
                            .constructorBuilder()
                            .addModifiers(Modifier.PRIVATE)
                            .build()
                        );
                    List<FieldSpec> fieldSpecs = new ArrayList<>();
                    List<MethodSpec> getterSpecs = new ArrayList<>();
                    parameters
                        .forEach(parameter -> {
                            TypeName typeName = ClassName.get(parameter.asType());
                            FieldSpec fieldSpec = FieldSpec
                                .builder(typeName, parameter.getSimpleName().toString(), Modifier.PRIVATE)
                                .build();
                            reqClass.addField(fieldSpec);
                            builderClass.addField(fieldSpec);
                            MethodSpec getter = MethodSpec
                                .methodBuilder("get" + StringUtils.capitalize(parameter.getSimpleName().toString()))
                                .addStatement("return $N", fieldSpec)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(typeName)
                                .build();
                            reqClass.addMethod(getter);

                            fieldSpecs.add(fieldSpec);
                            getterSpecs.add(getter);
                        });

                    List<ParameterSpec> constructorParameters = fieldSpecs
                        .stream()
                        .map(fieldSpec -> ParameterSpec.builder(fieldSpec.type, fieldSpec.name, Modifier.FINAL).build())
                        .collect(Collectors.toList());

                    MethodSpec.Builder constructor = MethodSpec
                        .constructorBuilder()
                        .addModifiers(Modifier.PRIVATE)
                        .addParameters(constructorParameters);
                    constructorParameters
                        .forEach(parameterSpec -> constructor.addStatement("this.$N = $N", parameterSpec, parameterSpec));
                    reqClass
                        .addMethod(constructor.build());

                    CodeBlock.Builder builderCode = CodeBlock.builder()
                        .add("return new $T(", reqClassName);
                    fieldSpecs.forEach(fieldSpec -> {
                        if (fieldSpecs.indexOf(fieldSpec) != 0) {
                            builderCode.add(" ,");
                        }
                        builderCode.add("$N", fieldSpec);
                    });
                    builderCode.add(");\n");
                    builderClass.addMethod(MethodSpec
                        .methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addCode(builderCode.build())
                        .returns(reqClassName)
                        .build()
                    );

                    fieldSpecs.forEach(fieldSpec -> {
                        builderClass.addMethod(MethodSpec
                            .methodBuilder(fieldSpec.name)
                            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                            .addParameter(ParameterSpec.builder(fieldSpec.type, fieldSpec.name).build())
                            .addStatement("this.$N = $N", fieldSpec, fieldSpec)
                            .build()
                        );
                    });

                    TypeSpec builderClassSpec = builderClass.build();
                    reqClass.addType(builderClassSpec);
                    reqClass.addMethod(MethodSpec
                        .methodBuilder("builder")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .returns(builderClassName)
                        .addStatement("return new $N()", builderClassSpec)
                        .build()
                    );

                    try {
                        JavaFile
                            .builder(packageName, reqClass.build())
                            .build()
                            .writeTo(filer);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
        return Sets.newHashSet();
    }
}
