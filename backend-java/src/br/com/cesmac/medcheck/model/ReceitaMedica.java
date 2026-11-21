package br.com.cesmac.medcheck.model;

import br.com.cesmac.medcheck.model.enums.StatusReceita;
import br.com.cesmac.medcheck.model.valueobjects.ItemReceita;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade Receita Médica Digital conforme [RF008] e [RF024].
 * Contém hash criptográfico SHA-256 e validação por QR Code.
 */
public class ReceitaMedica implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer atendimentoId;
    private Integer pacienteId;
    private Integer medicoId;
    private List<ItemReceita> itens;
    private String observacoes;
    private LocalDateTime dataEmissao;
    private String hashValidacao;
    private String urlQrCode;
    private StatusReceita status;

    public ReceitaMedica(Integer id, Integer atendimentoId, Integer pacienteId, Integer medicoId, String observacoes) {
        this.id = id;
        this.atendimentoId = atendimentoId;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.observacoes = observacoes;
        this.itens = new ArrayList<>();
        this.dataEmissao = LocalDateTime.now();
        this.status = StatusReceita.ATIVA;
        this.hashValidacao = gerarHashValidacao();
        this.urlQrCode = "/api/receitas/validar?hash=" + this.hashValidacao;
    }

    private String gerarHashValidacao() {
        try {
            String seed = id + "-" + pacienteId + "-" + medicoId + "-" + dataEmissao.toString() + "-" + System.nanoTime();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 32).toUpperCase();
        } catch (Exception e) {
            return "MEDCHECK-" + System.currentTimeMillis();
        }
    }

    public void adicionarItem(ItemReceita item) {
        if (item != null) {
            this.itens.add(item);
        }
    }

    public void cancelar() {
        this.status = StatusReceita.CANCELADA;
    }

    public void dispensar() {
        this.status = StatusReceita.DISPENSADA;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAtendimentoId() {
        return atendimentoId;
    }

    public Integer getPacienteId() {
        return pacienteId;
    }

    public Integer getMedicoId() {
        return medicoId;
    }

    public List<ItemReceita> getItens() {
        return itens;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public LocalDateTime getDataEmissao() {
        return dataEmissao;
    }

    public String getHashValidacao() {
        return hashValidacao;
    }

    public String getUrlQrCode() {
        return urlQrCode;
    }

    public StatusReceita getStatus() {
        return status;
    }

    public void setStatus(StatusReceita status) {
        this.status = status;
    }
}
