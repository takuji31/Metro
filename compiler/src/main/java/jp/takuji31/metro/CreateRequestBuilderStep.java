package jp.takuji31.metro;

import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;

import jp.takuji31.metro.annotation.APIClient;

public class CreateRequestBuilderStep implements ProcessingStep {

    private final Filer filer;

    public CreateRequestBuilderStep(Filer filer) {
        this.filer = filer;
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
            }
            Name simpleName = klass.getSimpleName();
            TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(simpleName + "APIClient");
            for (Element element : klass.getEnclosedElements()) {

            }
        }
        return Sets.newHashSet();
    }
}
