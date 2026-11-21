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
 * Implementação polimórfica para o Fluxograma de Dispneia / Falta de Ar (Manchester).
 */
public class ClassificadorDispneia implements IClassificadorManchester {

    @Override
    public String getNomeFluxograma() {
        return "Dispneia / Insuficiência Respiratória";
    }

    @Override
    public boolean isAplicavel(String queixaPrincipal) {
        if (queixaPrincipal == null) return false;
        String q = queixaPrincipal.toLowerCase();
        return q.contains("ar") || q.contains("dispne") || q.contains("respir") || q.contains("fôlego") || q.contains("falta de ar");
    }

    @Override
    public ClassificacaoRisco avaliar(Triagem triagem, HistoricoClinico historico) {
        SinaisVitais vitais = triagem.getSinaisVitais();

        // 1. Discriminador VERMELHO (Emergência: 0 min)
        if (vitais.isDessaturandoCritico() || (vitais.isTaquicardiaSevera() && triagem.getIntensidade() == IntensidadeSintoma.INTENSA)) {
            ClassificacaoRisco risco = new ClassificacaoRisco(
                    null, triagem.getId(), NivelPrioridade.EMERGENCIA, CorManchester.VERMELHO,
                    "Grave insuficiência respiratória detectada (SpO2 < 90%). Busque socorro de emergência imediato (SAMU 192).",
                    "Pronto-Socorro com Suporte Avançado de Vida / Oxigenoterapia.",
                    "Lábios ou pontas dos dedos arroxeados (cianose), confusão mental, incapacidade de falar frases inteiras."
            );
            risco.adicionarDiscriminador("Saturação de oxigênio criticamente baixa ou estridor respiratório.");
            return risco;
        }

        // 2. Discriminador LARANJA (Muito urgente: 10 min)
        if (vitais.isDessaturandoModerado() || triagem.getIntensidade() == IntensidadeSintoma.INTENSA) {
            ClassificacaoRisco risco = new ClassificacaoRisco(
                    null, triagem.getId(), NivelPrioridade.MUITO_URGENTE, CorManchester.LARANJA,
                    "Falta de ar acentuada. Mantenha posição sentada e aguarde atendimento médico prioritário.",
                    "Teleconsulta médica imediata ou Unidade de Pronto Atendimento em até 10 minutos.",
                    "Piora rápida da falta de ar ou dor no peito associada."
            );
            risco.adicionarDiscriminador("Dessaturação de oxigênio moderada (90-94%) ou grande esforço respiratório.");
            return risco;
        }

        // 3. Discriminador AMARELO (Urgente: 50 min)
        if (triagem.getIntensidade() == IntensidadeSintoma.MODERADA || vitais.isFebreModerada()) {
            ClassificacaoRisco risco = new ClassificacaoRisco(
                    null, triagem.getId(), NivelPrioridade.URGENTE, CorManchester.AMARELO,
                    "Falta de ar de intensidade moderada com sinais vitais aceitáveis. Mantenha repouso.",
                    "Atendimento médico em até 50 minutos.",
                    "Surgimento de chiado no peito ou piora ao deitar."
            );
            risco.adicionarDiscriminador("Dispneia moderada associada a sintomas respiratórios febris.");
            return risco;
        }

        // 4. Discriminador VERDE (Pouco urgente: 120 min)
        ClassificacaoRisco risco = new ClassificacaoRisco(
                null, triagem.getId(), NivelPrioridade.POUCO_URGENTE, CorManchester.VERDE,
                "Sensação de cansaço respiratório leve, sem queda de saturação de oxigênio.",
                "Avaliação ambulatorial preventiva.",
                "Tosse produtiva intensa com febre."
        );
        risco.adicionarDiscriminador("Falta de ar leve aos esforços sem sinais de alarme.");
        return risco;
    }
}
