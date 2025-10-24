package com.studentgest.user_service.service;

import com.studentgest.user_service.model.Rol;
import com.studentgest.user_service.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    public List<Rol> getAllRoles() {
        return rolRepository.findAll();
    }

    public Optional<Rol> getRolById(Integer id) {
        return rolRepository.findById(id);
    }

    public Rol createRol(Rol rol) {
        if (rolRepository.findByNombre(rol.getNombre()).isPresent()) {
            throw new RuntimeException("El rol " + rol.getNombre() + " ya existe");
        }
        return rolRepository.save(rol);
    }

    public Rol updateRol(Integer id, Rol rolDetails) {
        return rolRepository.findById(id).map(rol -> {
            rol.setNombre(rolDetails.getNombre());
            rol.setDescripcion(rolDetails.getDescripcion());
            return rolRepository.save(rol);
        }).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
    }

    public void deleteRol(Integer id) {
        rolRepository.deleteById(id);
    }

    public Optional<Rol> getRolByNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }
}