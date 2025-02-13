package com.es.aplicacion.util

import com.es.aplicacion.dto.UsuarioDTO
import com.es.aplicacion.dto.UsuarioRegisterDTO
import com.es.aplicacion.model.Direccion
import com.es.aplicacion.model.Usuario
import org.springframework.security.crypto.password.PasswordEncoder

object DTOMapper {

    fun usuarioRegisteredDTOToEntity(usuarioInsertDTO: UsuarioRegisterDTO, passwordEncoder: PasswordEncoder) : Usuario {
        return Usuario(
            _id = null,
            username = passwordEncoder.encode(usuarioInsertDTO.username),
            password = usuarioInsertDTO.password,
            email = usuarioInsertDTO.email,
            roles = usuarioInsertDTO.rol ?: "",
            direccion = Direccion(
                calle = usuarioInsertDTO.calle,
                num = usuarioInsertDTO.num,
                municipio = usuarioInsertDTO.municipio,
                provincia = usuarioInsertDTO.provincia,
                cp = usuarioInsertDTO.cp,
                ciudad = usuarioInsertDTO.ciudad
            ),
        )
    }

    fun entityToUsuarioRegisteredDTO(usuario: Usuario) : UsuarioRegisterDTO {

        return UsuarioRegisterDTO(
            username = usuario.username,
            email = usuario.email,
            password = usuario.password,
            passwordRepeat = usuario.password,
            rol = usuario.roles,
            calle = usuario.direccion.calle,
            num = usuario.direccion.num,
            municipio = usuario.direccion.municipio,
            provincia = usuario.direccion.provincia,
            cp = usuario.direccion.cp,
            ciudad = usuario.direccion.ciudad,
        )
    }

    fun usuarioEntityToUsuarioDTO(usuario: Usuario) : UsuarioDTO {
        return UsuarioDTO(
            username = usuario.username,
            email = usuario.email,
            rol = usuario.roles
        )
    }
}