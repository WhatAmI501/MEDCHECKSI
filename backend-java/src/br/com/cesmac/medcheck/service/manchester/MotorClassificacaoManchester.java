package br.com.cesmac.medcheck.service.manchester;

import br.com.cesmac.medcheck.model.ClassificacaoRisco;
import br.com.cesmac.medcheck.model.HistoricoClinico;
import br.com.cesmac.medcheck.model.Triagem;

import java.util.ArrayList;
import java.util.List;

/**
 * PILAR POO: POLIMORFISMO E ENCAPSULAMENTO
 * Motor determinístico do Protocolo de Manchester conforme [RF005] e [RNF13].
 * Orquestra as estratégias polimórficas especializadas de árvores de decisão clínica.
 */
public class MotorClassificacaoManchester {

    private final List<IClassificadorManchester> classificadores;

    public MotorClassificacaoManchester() {
        this.classificadores = new ArrayList<>();
        // Registra os classificadores específicos antes do classificador geral
        this.classificadores.add(new ClassificadorDorToracica());
        this.classificadores.add(new ClassificadorDispneia());
        this.classificadores.add(new ClassificadorCefaleia());
        this.classificadores.add(new ClassificadorGeral()); // Fallback polimórfico
    }

    /**
     * Executa a avaliação clínica determinística selecionando a estratégia adequada.
     */
    public ClassificacaoRisco processarClassificacao(Triagem triagem, HistoricoClinico historico) {
        if (triagem == null) {
            throw new IllegalArgumentException("Triagem não pode ser nula para processamento.");
        }

        String queixa = triagem.getQueixaPrincipal();
        IClassificadorManchester estrategiaEscolhida = null;

        for (IClassificadorManchester c : classificadores) {
            if (c.isAplicavel(queixa)) {
                estrategiaEscolhida = c;
                break;
            }
        }

        if (estrategiaEscolhida == null) {
            estrategiaEscolhida = new ClassificadorGeral();
        }

        ClassificacaoRisco resultado = estrategiaEscolhida.avaliar(triagem, historico);
        resultado.adicionarDiscriminador("Fluxograma acionado: " + estrategiaEscolhida.getNomeFluxograma());
        return resultado;
    }
}
