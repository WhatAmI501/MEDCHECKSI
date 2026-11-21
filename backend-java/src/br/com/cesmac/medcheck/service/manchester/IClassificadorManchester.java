package br.com.cesmac.medcheck.service.manchester;

import br.com.cesmac.medcheck.model.ClassificacaoRisco;
import br.com.cesmac.medcheck.model.HistoricoClinico;
import br.com.cesmac.medcheck.model.Triagem;

/**
 * PILAR POO: POLIMORFISMO E ABSTRAÇÃO (STRATEGY PATTERN)
 * Interface comum para os classificadores clínicos do Protocolo de Manchester [RF005 / RNF13].
 */
public interface IClassificadorManchester {
    /**
     * Avalia deterministamente os dados da triagem e histórico do paciente,
     * retornando a classificação de risco Manchester correspondente.
     */
    ClassificacaoRisco avaliar(Triagem triagem, HistoricoClinico historico);

    /**
     * Retorna o nome da árvore/fluxograma clínico atendido pela estratégia.
     */
    String getNomeFluxograma();

    /**
     * Informa se a estratégia é aplicável para a queixa relatada.
     */
    boolean isAplicavel(String queixaPrincipal);
}
