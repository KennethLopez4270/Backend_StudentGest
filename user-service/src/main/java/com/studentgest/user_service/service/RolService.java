package com.studentgest.user_service.service;

import com.studentgest.user_service.model.Funcionalidad;
import com.studentgest.user_service.model.Rol;
import com.studentgest.user_service.model.RolesFuncionalidades;
import com.studentgest.user_service.model.RolesFuncionalidadesId;
import com.studentgest.user_service.repository.FuncionalidadRepository;
import com.studentgest.user_service.repository.RolRepository;
import com.studentgest.user_service.repository.RolesFuncionalidadesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RolService {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private FuncionalidadRepository funcionalidadRepository;

    @Autowired
    private RolesFuncionalidadesRepository rolesFuncionalidadesRepository;

    @Transactional
    @jakarta.annotation.PostConstruct
    public void initializeDefaultRolesAndFunctionalities() {
        // 1. Crear Roles
        createRoleIfNotFound("Estudiante", "Rol para estudiantes");
        createRoleIfNotFound("Padre", "Rol para padres");
        Rol osiRole = createRoleIfNotFound("OSI", "Superadministrador con acceso total");
        createRoleIfNotFound("Profesor", "Rol para profesores");
        createRoleIfNotFound("Director", "Rol para directores");
        createRoleIfNotFound("Personal", "Rol para personal administrativo");

        // 2. Crear Funcionalidades Básicas (OSI)
        // Dashboard
        Funcionalidad dash = createFunctionalityIfNotFound("Dashboard Admin", "/admin-dashboard",
                "Dashboard principal para administradores", "DASH_001");

        // ABM Roles
        Funcionalidad roles = createFunctionalityIfNotFound("ABM Roles", "/abm-roles", "Gestión de Roles", "ROLES_001");

        // ABM Usuarios
        Funcionalidad usuarios = createFunctionalityIfNotFound("ABM Usuarios", "/abm-usuarios", "Gestión de Usuarios",
                "USER_001");

        // ABM Funcionalidades
        Funcionalidad funcs = createFunctionalityIfNotFound("ABM Funcionalidades", "/abm-funcionalidades",
                "Gestión de Funcionalidades", "FUNC_001");

        // Asignar Funcionalidades a Roles
        Funcionalidad assign = createFunctionalityIfNotFound("Asignar Roles", "/gestion-roles-funcionalidades",
                "Asignación de Permisos", "ASSIGN_001");

        // Reportes
        Funcionalidad reportes = createFunctionalityIfNotFound("Reportes Admin", "/admin-reports",
                "Reportes generales del sistema", "REP_001");

        // Gestión Padres
        Funcionalidad padres = createFunctionalityIfNotFound("Gestión Padres", "/gestion-padres",
                "Administración de padres de familia", "PADRES_001");

        // 3. Asignar todo a OSI
        if (osiRole != null) {
            assignFunctionalityToRoleIfNotExists(osiRole, dash);
            assignFunctionalityToRoleIfNotExists(osiRole, roles);
            assignFunctionalityToRoleIfNotExists(osiRole, usuarios);
            assignFunctionalityToRoleIfNotExists(osiRole, funcs);
            assignFunctionalityToRoleIfNotExists(osiRole, assign);
            assignFunctionalityToRoleIfNotExists(osiRole, reportes);
            assignFunctionalityToRoleIfNotExists(osiRole, padres);
            System.out.println("✅ Funcionalidades por defecto asignadas al rol OSI");
        }

    }

    private Rol createRoleIfNotFound(String nombre, String descripcion) {
        Optional<Rol> rolOpt = rolRepository.findByNombre(nombre);
        if (rolOpt.isEmpty()) {
            Rol rol = new Rol();
            rol.setNombre(nombre);
            rol.setDescripcion(descripcion);
            return rolRepository.save(rol);
        }
        return rolOpt.get();
    }

    private Funcionalidad createFunctionalityIfNotFound(String nombre, String direccion, String descripcion,
            String codigo) {
        // Asumiendo que existe findByCodigo o similar. Si no, buscamos por nombre o
        // dirección.
        // Como no tengo el repo a la vista, iteraré o asumiré que findByNombre existe o
        // uso stream.
        // Mejor añadir findByDireccion en repo? No puedo editar repo ahora facilmente.
        // Filtremos en memoria para el seed (no optimo pero seguro para init).
        return funcionalidadRepository.findAll().stream()
                .filter(f -> f.getDireccion().equals(direccion))
                .findFirst()
                .orElseGet(() -> {
                    Funcionalidad f = new Funcionalidad();
                    f.setNombre(nombre);
                    f.setDireccion(direccion);
                    f.setDescripcion(descripcion);
                    f.setCodigo(codigo);
                    return funcionalidadRepository.save(f);
                });
    }

    private void assignFunctionalityToRoleIfNotExists(Rol rol, Funcionalidad func) {
        RolesFuncionalidadesId id = new RolesFuncionalidadesId(rol.getIdRol(), func.getIdFuncionalidad());
        if (!rolesFuncionalidadesRepository.existsById(id)) {
            RolesFuncionalidades rf = new RolesFuncionalidades();
            rf.setId(id);
            rf.setRol(rol);
            rf.setFuncionalidad(func);
            rolesFuncionalidadesRepository.save(rf);
        }
    }

    public List<Map<String, Object>> getAllRoles() {
        return rolRepository.findAll().stream().map(rol -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id_rol", rol.getIdRol());
            map.put("nombre", rol.getNombre());
            map.put("descripcion", rol.getDescripcion());
            return map;
        }).collect(Collectors.toList());
    }

    public Optional<Rol> getRoleById(Integer id) {
        return rolRepository.findById(id);
    }

    public List<Map<String, Object>> getFunctionalitiesByRoleId(Integer id) {
        Rol rol = rolRepository.findById(id).orElse(null);
        if (rol == null)
            return List.of();

        return rol.getRolesFuncionalidades().stream()
                .map(rf -> {
                    Funcionalidad f = rf.getFuncionalidad();
                    Map<String, Object> map = new HashMap<>();
                    map.put("id_funcionalidad", f.getIdFuncionalidad());
                    map.put("nombre", f.getNombre());
                    map.put("descripcion", f.getDescripcion());
                    map.put("codigo", f.getCodigo());
                    map.put("direccion", f.getDireccion()); // ✅ Added
                    return map;
                })
                .collect(Collectors.toList());
    }

    public Rol createRole(Rol role) {
        return rolRepository.save(role);
    }

    public Rol updateRole(Integer id, Rol roleDetails) {
        return rolRepository.findById(id).map(rol -> {
            rol.setNombre(roleDetails.getNombre());
            rol.setDescripcion(roleDetails.getDescripcion());
            return rolRepository.save(rol);
        }).orElseThrow(() -> new RuntimeException("Rol no encontrado"));
    }

    public void deleteRole(Integer id) {
        rolRepository.deleteById(id);
    }

    public Funcionalidad createFunctionality(Funcionalidad funcionalidad) {
        return funcionalidadRepository.save(funcionalidad);
    }

    public Funcionalidad updateFunctionality(Integer id, Funcionalidad funcionalidadDetails) {
        return funcionalidadRepository.findById(id).map(func -> {
            func.setNombre(funcionalidadDetails.getNombre());
            func.setDescripcion(funcionalidadDetails.getDescripcion());
            func.setCodigo(funcionalidadDetails.getCodigo());
            return funcionalidadRepository.save(func);
        }).orElseThrow(() -> new RuntimeException("Funcionalidad no encontrada"));
    }

    public void deleteFunctionality(Integer id) {
        funcionalidadRepository.deleteById(id);
    }

    public void assignFunctionalityToRole(Integer roleId, Integer functionalityId) {
        Rol rol = rolRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        Funcionalidad func = funcionalidadRepository.findById(functionalityId)
                .orElseThrow(() -> new RuntimeException("Funcionalidad no encontrada"));

        RolesFuncionalidadesId id = new RolesFuncionalidadesId(roleId, functionalityId);
        if (rolesFuncionalidadesRepository.existsById(id)) {
            return; // Ya existe
        }

        RolesFuncionalidades rf = new RolesFuncionalidades();
        rf.setId(id);
        rf.setRol(rol);
        rf.setFuncionalidad(func);
        rolesFuncionalidadesRepository.save(rf);
    }

    public List<Map<String, Object>> getAllFunctionalities() {
        return funcionalidadRepository.findAll().stream().map(f -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id_funcionalidad", f.getIdFuncionalidad());
            map.put("nombre", f.getNombre());
            map.put("descripcion", f.getDescripcion());
            map.put("codigo", f.getCodigo());
            map.put("direccion", f.getDireccion()); // ✅ Added
            return map;
        }).collect(Collectors.toList());
    }

    public void removeFunctionalityFromRole(Integer roleId, Integer functionalityId) {
        RolesFuncionalidadesId id = new RolesFuncionalidadesId(roleId, functionalityId);
        if (rolesFuncionalidadesRepository.existsById(id)) {
            rolesFuncionalidadesRepository.deleteById(id);
        } else {
            throw new RuntimeException("Asignación no encontrada");
        }
    }
}
