package jp.takuji31.metro;

import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import jp.takuji31.metro.annotations.APIClient;

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
        return Sets.newHashSet(APIClient.class);
    }

    @Override
    public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
        Set<Element> elements = elementsByAnnotation.get(APIClient.class);
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
                    String methodName = element.getSimpleName().toString();
                    String reqClassName = StringUtils.capitalize(methodName) + "Request";
                    TypeSpec.Builder requestClassBuilder = TypeSpec.classBuilder(reqClassName);
                    TypeSpec.Builder requestClassBuilderBuilder = TypeSpec.classBuilder("Builder");
                    element
                        .getParameters()
                        .forEach(parameter -> {
                            TypeName typeName = ClassName.get(parameter.asType());
                            requestClassBuilder.addField(FieldSpec.builder(typeName, parameter.getSimpleName().toString(), Modifier.PRIVATE).build());
                        });
                    requestClassBuilder.addType(requestClassBuilderBuilder.build());
                    try {
                        JavaFile
                            .builder(elementUtils.getPackageOf(klass).toString(), requestClassBuilder.build())
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
