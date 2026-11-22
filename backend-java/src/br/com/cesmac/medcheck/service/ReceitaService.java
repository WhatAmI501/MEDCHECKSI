package br.com.cesmac.medcheck.service;

import br.com.cesmac.medcheck.model.*;
import br.com.cesmac.medcheck.model.enums.PerfilAcesso;
import br.com.cesmac.medcheck.model.valueobjects.ItemReceita;
import br.com.cesmac.medcheck.repository.MedCheckDataStore;

import java.util.ArrayList;
import java.util.List;

/**
 * Serviço de Prescrição Médica conforme [RF008, RF024, RF030].
 * Aplica as Regras Críticas de Segurança [RS002] e [RS007].
 */
public class ReceitaService {

    private final MedCheckDataStore dataStore;

    public ReceitaService() {
        this.dataStore = MedCheckDataStore.getInstance();
    }

    /**
     * Emite receita médica com checagem rigorosa de alérgenos [RS007] e perfil [RS002].
     */
    public ReceitaMedica emitirReceita(Integer atendimentoId, Integer pacienteId, Integer medicoId,
                                      List<ItemReceita> itens, String observacoes, boolean confirmacaoExplicitaAlergia) {
        // 1. Verificação de Perfil Médico
        Usuario usuario = dataStore.buscarUsuarioPorId(medicoId);
        if (usuario == null || usuario.getPerfil() != PerfilAcesso.MEDICO) {
            throw new SecurityException("Apenas profissionais médicos autenticados com registro profissional ativo podem emitir prescrições.");
        }

        if ((pacienteId == null || pacienteId <= 0) && atendimentoId != null && atendimentoId > 0) {
            AtendimentoMedico atendimento = dataStore.buscarAtendimentoPorId(atendimentoId);
            if (atendimento != null) {
                pacienteId = atendimento.getPacienteId();
            }
        }

        if (pacienteId == null || pacienteId <= 0) {
            throw new IllegalArgumentException("Paciente destinatário não identificado.");
        }

        if (atendimentoId == null || atendimentoId <= 0) {
            atendimentoId = dataStore.gerarProximoIdAtendimento();
            AtendimentoMedico novoAtendimento = new AtendimentoMedico(atendimentoId, null, pacienteId);
            novoAtendimento.assumirAtendimento(medicoId);
            dataStore.salvarAtendimento(novoAtendimento);
        }

        HistoricoClinico historico = dataStore.buscarHistoricoPorPaciente(pacienteId);

        // 2. Verificação Cruzada de Alergias
        List<String> conflitosAlergia = new ArrayList<>();
        if (historico != null && itens != null) {
            for (ItemReceita item : itens) {
                if (historico.possuiAlergiaA(item.getPrincipioAtivo()) || historico.possuiAlergiaA(item.getMedicamento())) {
                    conflitosAlergia.add(item.getMedicamento() + " (" + item.getPrincipioAtivo() + ")");
                }
            }
        }

        if (!conflitosAlergia.isEmpty() && !confirmacaoExplicitaAlergia) {
            String lista = String.join(", ", conflitosAlergia);
            throw new IllegalStateException("O paciente possui histórico declarado de hipersensibilidade ou alergia ao medicamento prescrito: "
                    + lista + ". É necessária confirmação clínica explícita para prosseguir.");
        }

        int receitaId = dataStore.gerarProximoIdReceita();
        ReceitaMedica receita = new ReceitaMedica(receitaId, atendimentoId, pacienteId, medicoId, observacoes);
        if (itens != null) {
            for (ItemReceita item : itens) {
                receita.adicionarItem(item);
            }
        }

        dataStore.salvarReceita(receita);
        dataStore.registrarLog(new LogAuditoria(null, medicoId, pacienteId, "RECEITA", "INSERCAO",
                "Receita #" + receitaId + " emitida com Hash: " + receita.getHashValidacao(), "127.0.0.1"));
        return receita;
    }

    public ReceitaMedica emitirReceita(Integer atendimentoId, Integer medicoId,
                                      List<ItemReceita> itens, String observacoes, boolean confirmacaoExplicitaAlergia) {
        return emitirReceita(atendimentoId, null, medicoId, itens, observacoes, confirmacaoExplicitaAlergia);
    }

    /**
     * Validação pública de autenticidade da receita médica por Hash/QR Code [RF024].
     */
    public ReceitaMedica validarReceitaPublica(String hashValidacao) {
        if (hashValidacao == null || hashValidacao.trim().isEmpty()) {
            return null;
        }
        return dataStore.buscarReceitaPorHash(hashValidacao.trim().toUpperCase());
    }
}
