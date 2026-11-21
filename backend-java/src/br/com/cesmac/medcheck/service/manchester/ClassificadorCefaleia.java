package br.com.cesmac.medcheck.service.manchester;

import br.com.cesmac.medcheck.model.ClassificacaoRisco;
import br.com.cesmac.medcheck.model.HistoricoClinico;
import br.com.cesmac.medcheck.model.Triagem;
import br.com.cesmac.medcheck.model.enums.CorManchester;
import br.com.cesmac.medcheck.model.enums.IntensidadeSintoma;
import br.com.cesmac.medcheck.model.enums.NivelPrioridade;
import br.com.cesmac.medcheck.model.valueobjects.SinaisVitais;

/**
 * PILAR POO: POLIMORFISMO
 * Implementação polimórfica para o Fluxograma de Cefaleia / Dor de Cabeça (Manchester).
 */
public class ClassificadorCefaleia implements IClassificadorManchester {

    @Override
    public String getNomeFluxograma() {
        return "Cefaleia / Distúrbios Neurológicos";
    }

    @Override
    public boolean isAplicavel(String queixaPrincipal) {
        if (queixaPrincipal == null) return false;
        String q = queixaPrincipal.toLowerCase();
        return q.contains("cabeça") || q.contains("cabeca") || q.contains("cefale") || q.contains("enxaquec");
    }

    @Override
    public ClassificacaoRisco avaliar(Triagem triagem, HistoricoClinico historico) {
        SinaisVitais vitais = triagem.getSinaisVitais();
        String sintomasTexto = String.join(" ", triagem.getSintomasAssociados()).toLowerCase();
        boolean rigidezNuca = sintomasTexto.contains("nuca") || sintomasTexto.contains("pescoço rígido");
        boolean vomitoJato = sintomasTexto.contains("vômito") || sintomasTexto.contains("vomito");
        boolean inicioRepentino = "REPENTINA".equalsIgnoreCase(triagem.getFrequencia());

        // 1. Discriminador VERMELHO (Emergência: 0 min)
        if (vitais.isCriseHipertensiva() && triagem.getIntensidade() == IntensidadeSintoma.INTENSA && inicioRepentino) {
            ClassificacaoRisco risco = new ClassificacaoRisco(
                    null, triagem.getId(), NivelPrioridade.EMERGENCIA, CorManchester.VERMELHO,
                    "Cefaleia súbita de forte intensidade com elevação pressórica grave. Procure atendimento emergencial imediatamente.",
                    "Pronto-Socorro Neurológico / UPA com tomografia.",
                    "Perda de força em um dos lados do corpo, fala arrastada, visão dupla."
            );
            risco.adicionarDiscriminador("Cefaleia em trovoada súbita associada a pico hipertensivo grave.");
            return risco;
        }

        // 2. Discriminador LARANJA (Muito urgente: 10 min)
        if (rigidezNuca || (triagem.getIntensidade() == IntensidadeSintoma.INTENSA && vomitoJato) || vitais.isFebreAlta()) {
            ClassificacaoRisco risco = new ClassificacaoRisco(
                    null, triagem.getId(), NivelPrioridade.MUITO_URGENTE, CorManchester.LARANJA,
                    "Dor de cabeça intensa com sinais meníngeos ou febre alta. Recomendada avaliação presencial imediata.",
                    "Atendimento Médico Urgente em até 10 minutos.",
                    "Alteração de consciência, confusão mental, manchas na pele."
            );
            risco.adicionarDiscriminador("Sinais meníngeos suspeitos ou cefaleia com hipertermia.");
            return risco;
        }

        // 3. Discriminador AMARELO (Urgente: 50 min)
        if (triagem.getIntensidade() == IntensidadeSintoma.MODERADA || vitais.isFebreModerada()) {
            ClassificacaoRisco risco = new ClassificacaoRisco(
                    null, triagem.getId(), NivelPrioridade.URGENTE, CorManchester.AMARELO,
                    "Cefaleia moderada persistente. Repouse em local escuro e silencioso, evitando telas.",
                    "Teleconsulta médica em até 50 minutos.",
                    "Piora progressiva, náuseas contínuas ou perturbação visual."
            );
            risco.adicionarDiscriminador("Cefaleia aguda moderada sem déficit neurológico focal.");
            return risco;
        }

        // 4. Discriminador VERDE (Pouco urgente: 120 min)
        ClassificacaoRisco risco = new ClassificacaoRisco(
                null, triagem.getId(), NivelPrioridade.POUCO_URGENTE, CorManchester.VERDE,
                "Dor de cabeça leve, sugestiva de tensão muscular ou fadiga.",
                "Tratamento sintomático e consulta ambulatorial.",
                "Não alívio após hidratação e repouso."
        );
        risco.adicionarDiscriminador("Cefaleia tensional leve crônica sem red flags.");
        return risco;
    }
}
