package com.umc.pediupatrao.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios") // Define a coleção no MongoDB
public class Usuario {
    @Id
    private String id;

    @NotBlank(message = "Informe o nome de usuário ou e-mail.")
    private String username; // Nome de usuário para login

    private String email;    // E-mail do usuário

    // ✅ Nunca é serializada em respostas JSON (só aceita gravação)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password; // Senha criptografada

    private String role;     // Papel do usuário, como ADMIN, ATENDENTE, GERENTE, FORNECEDOR

    public Usuario() {
    }

    public Usuario(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    
}
