package br.com.cesmac.medcheck.service;

import br.com.cesmac.medcheck.model.LogAuditoria;
import br.com.cesmac.medcheck.model.Usuario;
import br.com.cesmac.medcheck.repository.MedCheckDataStore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço de Autenticação e Controle de Sessão RBAC conforme [RF003, RF014, RS008].
 */
public class AuthService {

    private final MedCheckDataStore dataStore;
    private final Map<String, Integer> tentativasFalhasPorLogin = new ConcurrentHashMap<>();

    public AuthService() {
        this.dataStore = MedCheckDataStore.getInstance();
    }

    public Usuario autenticar(String identificador, String senha) {
        if (identificador == null || senha == null) {
            return null;
        }

        String login = identificador.trim();
        int falhas = tentativasFalhasPorLogin.getOrDefault(login, 0);
        if (falhas >= 5) {
            throw new SecurityException("[BLOQUEIO RS008] Conta temporariamente bloqueada por 5 tentativas inválidas de acesso.");
        }

        Usuario usuario = null;
        if (login.contains("@")) {
            usuario = dataStore.buscarUsuarioPorEmail(login);
        } else {
            String limpo = login.replaceAll("[^0-9]", "");
            usuario = dataStore.buscarUsuarioPorCpf(limpo);
        }

        if (usuario != null && usuario.autenticar(senha)) {
            tentativasFalhasPorLogin.remove(login);
            dataStore.registrarLog(new LogAuditoria(null, usuario.getId(), usuario.getId(), "AUTH", "LEITURA", "Login autenticado com sucesso", "127.0.0.1"));
            return usuario;
        }

        tentativasFalhasPorLogin.put(login, falhas + 1);
        dataStore.registrarLog(new LogAuditoria(null, null, null, "AUTH", "LEITURA", "Tentativa falha de login para: " + login, "127.0.0.1"));
        return null;
    }
}
