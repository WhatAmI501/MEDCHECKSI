package br.com.cesmac.medcheck.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidade de Trilha de Auditoria Imutável conforme [RF032], [RS006] e LGPD.
 */
public class LogAuditoria implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer usuarioId;
    private Integer pacienteAfetadoId;
    private String moduloAcao;
    private String tipoOperacao; // LEITURA, INSERCAO, ATUALIZACAO, EXPORTACAO
    private String detalhes;
    private String ipOrigem;
    private LocalDateTime dataHora;

    public LogAuditoria(Integer id, Integer usuarioId, Integer pacienteAfetadoId,
                        String moduloAcao, String tipoOperacao, String detalhes, String ipOrigem) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.pacienteAfetadoId = pacienteAfetadoId;
        this.moduloAcao = moduloAcao;
        this.tipoOperacao = tipoOperacao;
        this.detalhes = detalhes;
        this.ipOrigem = ipOrigem != null ? ipOrigem : "127.0.0.1";
        this.dataHora = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public Integer getPacienteAfetadoId() {
        return pacienteAfetadoId;
    }

    public String getModuloAcao() {
        return moduloAcao;
    }

    public String getTipoOperacao() {
        return tipoOperacao;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public String getIpOrigem() {
        return ipOrigem;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
