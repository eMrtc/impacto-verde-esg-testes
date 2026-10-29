package br.com.impactoverde.api.config;

/** Perfis de acesso (RBAC mínimo): ADMIN pode tudo que OPERADOR pode, e mais um pouco. */
public enum Perfil {
    OPERADOR,
    ADMIN
}
