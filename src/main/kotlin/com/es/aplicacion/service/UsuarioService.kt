package com.es.aplicacion.service

import com.es.aplicacion.dto.UsuarioDTO
import com.es.aplicacion.dto.UsuarioRegisterDTO
import com.es.aplicacion.error.exception.BadRequestException
import com.es.aplicacion.error.exception.NotFoundException
import com.es.aplicacion.error.exception.UnauthorizedException
import com.es.aplicacion.model.Usuario
import com.es.aplicacion.repository.UsuarioRepository
import com.es.aplicacion.util.DTOMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class UsuarioService : UserDetailsService {

    @Autowired
    private lateinit var usuarioRepository: UsuarioRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    private lateinit var apiService: ExternalApiService


    override fun loadUserByUsername(username: String?): UserDetails {
        var usuario: Usuario = usuarioRepository
            .findByUsername(username!!)
            .orElseThrow {
                UnauthorizedException("$username no existente")
            }

        return User.builder()
            .username(usuario.username)
            .password(usuario.password)
            .roles(usuario.roles)
            .build()
    }

    fun insertUser(usuarioInsertadoDTO: UsuarioRegisterDTO) : UsuarioDTO? {

        if(usuarioInsertadoDTO.username.isBlank()
            || usuarioInsertadoDTO.email.isBlank()
            || usuarioInsertadoDTO.password.isBlank()
            || usuarioInsertadoDTO.passwordRepeat.isBlank()){

            throw BadRequestException("Uno o más campos vacios")
        }

        if(usuarioRepository.findByUsername(usuarioInsertadoDTO.username).isPresent){
            throw Exception("Usuario ${usuarioInsertadoDTO.username} ya está registrado")
        }

        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

        if (!emailRegex.matches(usuarioInsertadoDTO.email)) {
            throw BadRequestException("El email no es válido")
        }

        if (usuarioInsertadoDTO.password != usuarioInsertadoDTO.passwordRepeat) {
            throw BadRequestException("Las contraseñas no coinciden")
        }

        val usuario = DTOMapper.usuarioRegisteredDTOToEntity(usuarioInsertadoDTO, passwordEncoder)
        val datosProvincias = apiService.obtenerDatosDesdeApi()

        var cpro = ""

        if(datosProvincias?.data != null) {
            datosProvincias.data.stream().filter {
                if (it.PRO == usuario.direccion.provincia.uppercase()) {
                    cpro = it.CPRO
                }
                it.PRO == usuario.direccion.provincia.uppercase()
            }.findFirst().orElseThrow {
                NotFoundException("Provincia ${usuario.direccion.provincia.uppercase()} no válida.")
            }
        }

        val datosMunicipios = apiService.obtenerMunicipios(cpro)

        if(datosMunicipios?.data != null) {
            datosMunicipios.data.stream().filter {
                it.DMUN50 == usuario.direccion.municipio.uppercase()
            }.findFirst().orElseThrow {
                NotFoundException("Municipio ${usuario.direccion.municipio.uppercase()} no válido.")
            }
        }

        usuarioRepository.insert(usuario)

        return DTOMapper.usuarioEntityToUsuarioDTO(usuario)
    }
}