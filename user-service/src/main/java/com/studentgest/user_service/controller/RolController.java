package com.studentgest.user_service.controller;

import com.studentgest.user_service.model.Funcionalidad;
import com.studentgest.user_service.model.Rol;
import com.studentgest.user_service.service.AuditLogService;
import com.studentgest.user_service.service.RolService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
public class RolController {

    private static final Logger logger = LoggerFactory.getLogger(RolController.class);

    @Autowired
    private RolService rolService;

    @Autowired
    private AuditLogService auditLogService;

    // Initialize default roles and functionalities
    @PostMapping("/initialize")
    public Map<String, String> initializeRolesAndFunctionalities() {
        try {
            rolService.initializeDefaultRolesAndFunctionalities();
            return Map.of("message", "Roles y funcionalidades inicializados correctamente");
        } catch (Exception e) {
            logger.error("Error al inicializar roles y funcionalidades", e);
            return Map.of("message", "Error al inicializar: " + e.getMessage());
        }
    }

    // Get all roles
    @GetMapping
    public List<Map<String, Object>> getAllRoles() {
        try {
            return rolService.getAllRoles();
        } catch (Exception e) {
            logger.error("Error al obtener roles", e);
            return List.of();
        }
    }

    // Get role by ID
    @GetMapping("/{id}")
    public Map<String, Object> getRoleById(@PathVariable Integer id) {
        try {
            Optional<Rol> role = rolService.getRoleById(id);
            if (role.isPresent()) {
                Rol r = role.get();
                Map<String, Object> roleMap = new HashMap<>();
                roleMap.put("id_rol", r.getIdRol());
                roleMap.put("nombre", r.getNombre());
                roleMap.put("descripcion", r.getDescripcion());
                return roleMap;
            }
            return Map.of("message", "Rol no encontrado");
        } catch (Exception e) {
            logger.error("Error al obtener rol con ID: {}", id, e);
            return Map.of("message", "Error al obtener rol");
        }
    }

    // Get functionalities by role ID
    @GetMapping("/{id}/functionalities")
    public List<Map<String, Object>> getFunctionalitiesByRoleId(@PathVariable Integer id) {
        try {
            return rolService.getFunctionalitiesByRoleId(id);
        } catch (Exception e) {
            logger.error("Error al obtener funcionalidades para el rol con ID: {}", id, e);
            return List.of();
        }
    }

    // Create a new role
    @PostMapping
    public Rol createRole(@RequestBody Rol role, HttpServletRequest request) {
        try {
            Rol createdRole = rolService.createRole(role);
            // ✅ LOG DE SEGURIDAD: Creación de rol
            auditLogService.logRoleCreated(createdRole.getNombre(), null, getClientIp(request));
            return createdRole;
        } catch (Exception e) {
            logger.error("Error al crear rol", e);
            throw new RuntimeException("Error al crear rol: " + e.getMessage());
        }
    }

    // Update a role
    @PutMapping("/{id}")
    public Rol updateRole(@PathVariable Integer id, @RequestBody Rol roleDetails, HttpServletRequest request) {
        try {
            Rol updatedRole = rolService.updateRole(id, roleDetails);
            // ✅ LOG DE SEGURIDAD: Modificación de rol
            auditLogService.logRoleModified(id, updatedRole.getNombre(), null, getClientIp(request));
            return updatedRole;
        } catch (Exception e) {
            logger.error("Error al actualizar rol con ID: {}", id, e);
            throw new RuntimeException("Error al actualizar rol: " + e.getMessage());
        }
    }

    // Delete a role
    @DeleteMapping("/{id}")
    public Map<String, String> deleteRole(@PathVariable Integer id, HttpServletRequest request) {
        try {
            rolService.deleteRole(id);
            // ✅ LOG DE SEGURIDAD: Eliminación de rol
            auditLogService.logRoleDeleted(id, null, getClientIp(request));
            return Map.of("message", "Rol eliminado correctamente");
        } catch (Exception e) {
            logger.error("Error al eliminar rol con ID: {}", id, e);
            return Map.of("message", "Error al eliminar rol: " + e.getMessage());
        }
    }

    // Create a new functionality
    @PostMapping("/functionalities")
    public Funcionalidad createFunctionality(@RequestBody Funcionalidad funcionalidad, HttpServletRequest request) {
        try {
            Funcionalidad created = rolService.createFunctionality(funcionalidad);
            // ✅ LOG DE SEGURIDAD: Creación de funcionalidad
            auditLogService.logFunctionalityCreated(created.getNombre(), null, getClientIp(request));
            return created;
        } catch (Exception e) {
            logger.error("Error al crear funcionalidad", e);
            throw new RuntimeException("Error al crear funcionalidad: " + e.getMessage());
        }
    }

    // Update a functionality
    @PutMapping("/functionalities/{id}")
    public Funcionalidad updateFunctionality(@PathVariable Integer id, @RequestBody Funcionalidad funcionalidad) {
        try {
            return rolService.updateFunctionality(id, funcionalidad);
        } catch (Exception e) {
            logger.error("Error al actualizar funcionalidad con ID: {}", id, e);
            throw new RuntimeException("Error al actualizar funcionalidad: " + e.getMessage());
        }
    }

    // Delete a functionality
    @DeleteMapping("/functionalities/{id}")
    public Map<String, String> deleteFunctionality(@PathVariable Integer id, HttpServletRequest request) {
        try {
            rolService.deleteFunctionality(id);
            // ✅ LOG DE SEGURIDAD: Eliminación de funcionalidad
            auditLogService.logFunctionalityDeleted(id, null, getClientIp(request));
            return Map.of("message", "Funcionalidad eliminada correctamente");
        } catch (Exception e) {
            logger.error("Error al eliminar funcionalidad con ID: {}", id, e);
            return Map.of("message", "Error al eliminar funcionalidad: " + e.getMessage());
        }
    }

    // Assign functionality to role
    @PostMapping("/{roleId}/functionalities/{functionalityId}")
    public Map<String, String> assignFunctionalityToRole(@PathVariable Integer roleId,
            @PathVariable Integer functionalityId, HttpServletRequest request) {
        try {
            rolService.assignFunctionalityToRole(roleId, functionalityId);
            // ✅ LOG DE SEGURIDAD: Asignación de funcionalidad a rol
            auditLogService.logFunctionalityAssigned(roleId, functionalityId, null, getClientIp(request));
            return Map.of("message", "Funcionalidad asignada al rol correctamente");
        } catch (Exception e) {
            logger.error("Error al asignar funcionalidad {} al rol {}", functionalityId, roleId, e);
            return Map.of("message", "Error al asignar funcionalidad: " + e.getMessage());
        }
    }

    // GET ALL FUNCTIONALITIES
    @GetMapping("/functionalities")
    public List<Map<String, Object>> getAllFunctionalities() {
        try {
            return rolService.getAllFunctionalities();
        } catch (Exception e) {
            logger.error("Error al obtener todas las funcionalidades", e);
            return List.of();
        }
    }

    // Remove functionality from role
    @DeleteMapping("/{roleId}/functionalities/{functionalityId}")
    public Map<String, String> removeFunctionalityFromRole(
            @PathVariable Integer roleId,
            @PathVariable Integer functionalityId,
            HttpServletRequest request) {
        try {
            rolService.removeFunctionalityFromRole(roleId, functionalityId);
            // ✅ LOG DE SEGURIDAD: Remoción de funcionalidad de rol
            auditLogService.logFunctionalityRemoved(roleId, functionalityId, null, getClientIp(request));
            return Map.of("message", "Funcionalidad desasignada correctamente");
        } catch (Exception e) {
            logger.error("Error al desasignar funcionalidad {} del rol {}", functionalityId, roleId, e);
            return Map.of("message", "Error al desasignar: " + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
