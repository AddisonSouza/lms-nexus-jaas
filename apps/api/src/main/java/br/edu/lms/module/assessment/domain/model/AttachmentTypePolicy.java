package br.edu.lms.module.assessment.domain.model;

import br.edu.lms.module.assessment.domain.exception.InvalidAttachmentTypeException;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * O que o aluno pode anexar a uma resposta: documento, imagem ou pacote —
 * nada que o servidor guarde e alguém possa baixar e executar.
 *
 * <p>Extensão <b>e</b> MIME precisam estar na lista. Só o MIME não basta, pois
 * quem envia declara o que quiser no cabeçalho; só a extensão também não, porque
 * renomear um arquivo é trivial. Exigir os dois não prova o conteúdo — isso
 * seria trabalho de magic bytes, fora deste escopo —, mas fecha os dois enganos
 * de uma linha.
 *
 * <p>Vive no domínio, e não no serviço, porque a regra é a mesma em qualquer
 * caminho que grave um anexo de submissão.
 */
public final class AttachmentTypePolicy {

    /** Extensão → MIMEs aceitos para ela. */
    private static final Map<String, Set<String>> ALLOWED = Map.of(
            "pdf", Set.of("application/pdf"),
            "doc", Set.of("application/msword"),
            "docx", Set.of("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
            "zip", Set.of("application/zip", "application/x-zip-compressed"),
            "jpg", Set.of("image/jpeg"),
            "jpeg", Set.of("image/jpeg"),
            "png", Set.of("image/png"));

    private AttachmentTypePolicy() {
    }

    /**
     * Recusa o anexo cuja extensão ou MIME esteja fora da lista.
     *
     * @throws InvalidAttachmentTypeException com o tipo recusado, que a API
     *         devolve como 422.
     */
    public static void validate(String fileName, String mimeType) {
        var allowedMimes = ALLOWED.get(extensionOf(fileName));
        if (allowedMimes == null || mimeType == null || !allowedMimes.contains(normalize(mimeType))) {
            throw new InvalidAttachmentTypeException(mimeType);
        }
    }

    /**
     * Extensão em minúsculas, sem o ponto. Arquivo sem extensão, terminado em
     * ponto ou oculto (`.gitignore`) devolve vazio — e vazio nunca está na
     * lista, então é recusado.
     */
    private static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        var dot = fileName.lastIndexOf('.');
        return dot <= 0 || dot == fileName.length() - 1
                ? ""
                : normalize(fileName.substring(dot + 1));
    }

    /** O MIME pode vir com parâmetro (`image/png; charset=binary`) e em maiúsculas. */
    private static String normalize(String value) {
        var withoutParameters = value.split(";")[0];
        return withoutParameters.trim().toLowerCase(Locale.ROOT);
    }
}
