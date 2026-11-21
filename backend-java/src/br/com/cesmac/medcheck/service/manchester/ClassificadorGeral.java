package br.com.cesmac.medcheck.service.manchester;

import br.com.cesmac.medcheck.model.ClassificacaoRisco;
import br.com.cesmac.medcheck.model.HistoricoClinico;
import br.com.cesmac.medcheck.model.Triagem;
import br.com.cesmac.medcheck.model.enums.CorManchester;
import br.com.cesmac.medcheck.model.enums.IntensidadeSintoma;
import br.com.cesmac.medcheck.model.enums.NivelPrioridade;
import br.com.cesmac.medcheck.model.valueobjects.SinaisVitais;

/**
 * PILAR POO: POLIMORFISMO (FALLBACK / SINTOMAS GERAIS)
 * Implementação padrão para queixas clínicas gerais não cobertas por fluxogramas específicos.
 */
public class ClassificadorGeral implements IClassificadorManchester {

    @Override
    public String getNomeFluxograma() {
        return "Clínica Médica Geral / Mal-Estar";
    }

    @Override
    public boolean isAplicavel(String queixaPrincipal) {
        return true; // Aceita qualquer queixa como estratégia padrão
    }

    @Override
    public ClassificacaoRisco avaliar(Triagem triagem, HistoricoClinico historico) {
        SinaisVitais vitais = triagem.getSinaisVitais();

        if (vitais.isDessaturandoCritico() || vitais.isCriseHipertensiva()) {
            ClassificacaoRisco risco = new ClassificacaoRisco(
                    null, triagem.getId(), NivelPrioridade.EMERGENCIA, CorManchester.VERMELHO,
                    "Alteração crítica detectada nos parâmetros vitais autoinformados. Busque socorro urgente.",
                    "Serviço de Urgência e Emergência (SAMU 192 ou UPA 24h).",
                    "Perda de consciência ou piora acentuada."
            );
            risco.adicionarDiscriminador("Sinais vitais com risco iminente de colapso.");
            return risco;
        }

        if (triagem.getIntensidade() == IntensidadeSintoma.INTENSA || vitais.isFebreAlta()) {
            ClassificacaoRisco risco = new ClassificacaoRisco(
                    null, triagem.getId(), NivelPrioridade.MUITO_URGENTE, CorManchester.LARANJA,
                    "Quadro clínico agudo com intensidade severa ou febre alta (>39.5°C).",
                    "Teleatendimento médico prioritário em até 10 minutos.",
                    "Sintomas associados incapacitantes."
            );
            risco.adicionarDiscriminador("Dor ou mal-estar severo incapacitante / hipertermia grave.");
            return risco;
        }

        if (triagem.getIntensidade() == IntensidadeSintoma.MODERADA || vitais.isFebreModerada()) {
            ClassificacaoRisco risco = new ClassificacaoRisco(
                    null, triagem.getId(), NivelPrioridade.URGENTE, CorManchester.AMARELO,
                    "Quadro sintomático moderado. Mantenha repouso e hidratação oral.",
                    "Teleconsulta médica em até 50 minutos.",
                    "Persistência dos sintomas por mais de 72 horas."
            );
            risco.adicionarDiscriminador("Sintomatologia moderada com estabilidade hemodinâmica.");
            return risco;
        }

        ClassificacaoRisco risco = new ClassificacaoRisco(
                null, triagem.getId(), NivelPrioridade.POUCO_URGENTE, CorManchester.VERDE,
                "Sintomas brandos sem critérios clínicos de urgência.",
                "Orientação domiciliar e consulta ambulatorial.",
                "Evolução desfavorável dos sintomas."
        );
        risco.adicionarDiscriminador("Quadro clínico leve e estável.");
        return risco;
    }
}
