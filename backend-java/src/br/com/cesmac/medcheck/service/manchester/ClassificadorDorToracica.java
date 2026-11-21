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
 * Implementação polimórfica para o Fluxograma de Dor Torácica (Manchester).
 */
public class ClassificadorDorToracica implements IClassificadorManchester {

    @Override
    public String getNomeFluxograma() {
        return "Dor Torácica / Síndrome Coronariana";
    }

    @Override
    public boolean isAplicavel(String queixaPrincipal) {
        if (queixaPrincipal == null) return false;
        String q = queixaPrincipal.toLowerCase();
        return q.contains("torac") || q.contains("peito") || q.contains("coracao") || q.contains("cardíac");
    }

    @Override
    public ClassificacaoRisco avaliar(Triagem triagem, HistoricoClinico historico) {
        SinaisVitais vitais = triagem.getSinaisVitais();
        boolean cardiopata = historico != null && historico.isPossuiCardiopatia();
        String sintomasTexto = String.join(" ", triagem.getSintomasAssociados()).toLowerCase();
        boolean dorIrradiada = sintomasTexto.contains("braço") || sintomasTexto.contains("mandíbula") || sintomasTexto.contains("costas");
        boolean sudorese = sintomasTexto.contains("sudorese") || sintomasTexto.contains("suor frio");

        // 1. Discriminador VERMELHO (Emergência imediata: 0 min)
        if (vitais.isDessaturandoCritico() || vitais.isBradicardiaSevera() ||
            (triagem.getIntensidade() == IntensidadeSintoma.INTENSA && vitais.isCriseHipertensiva() && dorIrradiada)) {
            ClassificacaoRisco risco = new ClassificacaoRisco(
                    null, triagem.getId(), NivelPrioridade.EMERGENCIA, CorManchester.VERMELHO,
                    "Procure imediatamente o pronto-socorro mais próximo ou acione o SAMU 192. Risco de evento coronariano agudo grave.",
                    "Encaminhamento imediato a Emergência Cardiológica / Hospitalar.",
                    "Desmaio, perda de consciência, sudorese intensa, dor irradiada insuportável."
            );
            risco.adicionarDiscriminador("Comprometimento hemodinâmico crítico ou dor anginosa severa com choque/hipóxia.");
            return risco;
        }

        // 2. Discriminador LARANJA (Muito urgente: 10 min)
        if (triagem.getIntensidade() == IntensidadeSintoma.INTENSA || dorIrradiada || sudorese || cardiopata || vitais.isDessaturandoModerado()) {
            ClassificacaoRisco risco = new ClassificacaoRisco(
                    null, triagem.getId(), NivelPrioridade.MUITO_URGENTE, CorManchester.LARANJA,
                    "Dor precordial com características suspeitas. Permaneça em repouso e conecte-se à fila prioritária de teleatendimento médico.",
                    "Teleconsulta prioritária em até 10 minutos ou Pronto Atendimento.",
                    "Falta de ar progressiva, vômitos em jato, piora da dor no peito."
            );
            risco.adicionarDiscriminador("Dor típica precordial com irradiação ou fatores de risco cardiovascular associados.");
            return risco;
        }

        // 3. Discriminador AMARELO (Urgente: 50 min)
        if (triagem.getIntensidade() == IntensidadeSintoma.MODERADA || vitais.isFebreModerada()) {
            ClassificacaoRisco risco = new ClassificacaoRisco(
                    null, triagem.getId(), NivelPrioridade.URGENTE, CorManchester.AMARELO,
                    "Dor torácica de intensidade moderada sem sinais de choque hemodinâmico. Mantenha repouso.",
                    "Avaliação médica em teleconsulta ou UPA em até 50 minutos.",
                    "Surgimento de palpitações ou falta de ar."
            );
            risco.adicionarDiscriminador("Dor torácica moderada sem irradiação com sinais vitais estáveis.");
            return risco;
        }

        // 4. Discriminador VERDE (Pouco urgente: 120 min)
        ClassificacaoRisco risco = new ClassificacaoRisco(
                null, triagem.getId(), NivelPrioridade.POUCO_URGENTE, CorManchester.VERDE,
                "Desconforto torácico leve, provavelmente musculoesquelético. Evite esforços físicos.",
                "Consulta ambulatorial em Unidade Básica de Saúde ou teleconsulta agendada.",
                "Piora da intensidade ou irradiação da dor."
        );
        risco.adicionarDiscriminador("Desconforto torácico atípico, leve e localizado.");
        return risco;
    }
}
