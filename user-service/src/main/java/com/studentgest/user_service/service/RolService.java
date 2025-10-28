package com.studentgest.user_service.service;

import com.studentgest.user_service.model.*;
import com.studentgest.user_service.model.EstadoUsuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RolService {

    private static final Logger logger = LoggerFactory.getLogger(RolService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private static final String OSI_ROLE_NAME = "OSI";
    private static final Integer OSI_ROLE_ID = 1; // Assuming OSI role has ID 1
    private static final List<Map<String, String>> DEFAULT_FUNCTIONALITIES = Arrays.asList(
            Map.of("path", "/abm-usuarios", "name", "ABMUsuarios", "component", "ABMUsuariosView", "description", "Gestión de usuarios"),
            Map.of("path", "/abm-roles", "name", "ABMRoles", "component", "ABMRolesView", "description", "Gestión de roles"),
            Map.of("path", "/abm-funcionalidades", "name", "ABMFuncionalidades", "component", "ABMFuncionalidadesView", "description", "Gestión de funcionalidades"),
            Map.of("path", "/gestion-roles-funcionalidades", "name", "GestionRolesFuncionalidades", "component", "GestionRolesFuncionalidadesView", "description", "Gestión de roles y funcionalidades")
    );

    // Initialize default roles, functionalities, and OSI user
    @Transactional
    public void initializeDefaultRolesAndFunctionalities() {
        try {
            Integer osiRoleId = entityManager.createQuery(
                            "SELECT r.idRol FROM Rol r WHERE r.nombre = :nombre", Integer.class)
                    .setParameter("nombre", OSI_ROLE_NAME)
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (osiRoleId == null) {
                Rol osiRole = new Rol();
                osiRole.setNombre(OSI_ROLE_NAME);
                osiRole.setDescripcion("Rol administrador del sistema OSI");
                entityManager.persist(osiRole);
                entityManager.flush(); // Asegura que el ID se genere
                osiRoleId = osiRole.getIdRol();
                logger.info("OSI role created with ID: {}", osiRoleId);
            }

            for (Map<String, String> funcData : DEFAULT_FUNCTIONALITIES) {
                String path = funcData.get("path");
                Integer funcId = entityManager.createQuery(
                                "SELECT f.idFuncionalidad FROM Funcionalidad f WHERE f.direccion = :direccion", Integer.class)
                        .setParameter("direccion", path)
                        .getResultList()
                        .stream()
                        .findFirst()
                        .orElse(null);

                if (funcId == null) {
                    Funcionalidad funcionalidad = new Funcionalidad();
                    funcionalidad.setNombre(funcData.get("name"));
                    funcionalidad.setDescripcion(funcData.get("description"));
                    funcionalidad.setDireccion(funcData.get("path"));
                    entityManager.persist(funcionalidad);
                    entityManager.flush(); // Asegura que el ID se genere
                    funcId = funcionalidad.getIdFuncionalidad();
                    logger.info("Funcionalidad creada con ID: {}", funcId);
                }

                List<RolesFuncionalidades> existingAssignments = entityManager.createQuery(
                                "SELECT rf FROM RolesFuncionalidades rf WHERE rf.id.idRol = :roleId AND rf.id.idFuncionalidad = :funcId",
                                RolesFuncionalidades.class)
                        .setParameter("roleId", osiRoleId)
                        .setParameter("funcId", funcId)
                        .getResultList();

                if (existingAssignments.isEmpty()) {
                    RolesFuncionalidades rf = new RolesFuncionalidades();
                    rf.setId(new RolesFuncionalidadesId(osiRoleId, funcId));
                    entityManager.persist(rf);
                    entityManager.flush(); // Asegura que la asignación se complete
                    logger.info("Asignación creada: rol ID {}, funcionalidad ID {}", osiRoleId, funcId);
                }
            }

            // Inicializar usuario OSI
            Integer osiUserId = entityManager.createQuery(
                            "SELECT u.id_usuario FROM User u WHERE u.email = :email", Integer.class)
                    .setParameter("email", "osi@system.com")
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (osiUserId == null) {
                User osiUser = new User();
                osiUser.setNombre("OSI");
                osiUser.setApellido_paterno("Admin");
                osiUser.setApellido_materno("System");
                osiUser.setEmail("osi@system.com");
                osiUser.setPassword("password123"); // Cambia esto a un hash seguro en producción
                osiUser.setFoto("default.jpg");
                osiUser.setEstado(EstadoUsuario.APROBADO); // Ajusta según tu enum EstadoUsuario
                osiUser.setActivo(true);
                osiUser.setCreado_en(LocalDateTime.now());
                osiUser.setId_rol(osiRoleId); // Asigna el id_rol del rol OSI
                entityManager.persist(osiUser);
                entityManager.flush();
                osiUserId = osiUser.getId_usuario();
                logger.info("Usuario OSI creado con ID: {}", osiUserId);
            }

        } catch (Exception e) {
            logger.error("Error al inicializar roles, funcionalidades y usuario por defecto", e);
            throw new RuntimeException("Error al inicializar: " + e.getMessage(), e);
        }
    }

    // Get all roles
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllRoles() {
        try {
            List<Object[]> results = entityManager.createQuery(
                            "SELECT r.idRol, r.nombre, r.descripcion FROM Rol r", Object[].class)
                    .getResultList();

            List<Map<String, Object>> roles = new ArrayList<>();
            for (Object[] result : results) {
                Map<String, Object> roleMap = new HashMap<>();
                roleMap.put("idRol", result[0]);
                roleMap.put("nombre", result[1]);
                roleMap.put("descripcion", result[2]);
                roles.add(roleMap);
            }
            return roles;
        } catch (Exception e) {
            logger.error("Error al obtener todos los roles", e);
            return List.of();
        }
    }

    // Get role by ID
    @Transactional(readOnly = true)
    public Optional<Rol> getRoleById(Integer id) {
        try {
            return Optional.ofNullable(entityManager.find(Rol.class, id));
        } catch (Exception e) {
            logger.error("Error al obtener rol con ID: {}", id, e);
            return Optional.empty();
        }
    }

    // Get functionalities by role ID (updated to include only id and direccion)
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getFunctionalitiesByRoleId(Integer roleId) {
        try {
            List<RolesFuncionalidades> roleFuncs = entityManager.createQuery(
                            "SELECT rf FROM RolesFuncionalidades rf WHERE rf.id.idRol = :roleId", RolesFuncionalidades.class)
                    .setParameter("roleId", roleId)
                    .getResultList();

            return roleFuncs.stream().map(rf -> {
                Map<String, Object> funcMap = new HashMap<>();
                funcMap.put("id_funcionalidad", rf.getId().getIdFuncionalidad());
                funcMap.put("direccion", rf.getFuncionalidad().getDireccion()); // Solo dirección ahora
                return funcMap;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error al obtener funcionalidades para el rol con ID: {}", roleId, e);
            return List.of();
        }
    }

    // Create a new role
    @Transactional
    public Rol createRole(Rol role) {
        try {
            if (isRoleNameOrDescriptionTaken(role.getNombre(), role.getDescripcion(), null)) {
                throw new IllegalArgumentException("El nombre o descripción del rol ya está en uso");
            }
            if (role.getNombre().equalsIgnoreCase(OSI_ROLE_NAME)) {
                throw new IllegalArgumentException("No se puede crear un rol con el nombre OSI");
            }
            entityManager.persist(role);
            logger.info("Rol creado: {}", role.getNombre());
            return role;
        } catch (Exception e) {
            logger.error("Error al crear rol: {}", role.getNombre(), e);
            throw new RuntimeException("Error al crear rol: " + e.getMessage());
        }
    }

    // Update an existing role
    @Transactional
    public Rol updateRole(Integer id, Rol roleDetails) {
        try {
            if (id.equals(OSI_ROLE_ID)) {
                throw new IllegalStateException("No se puede modificar el rol OSI");
            }
            Optional<Rol> roleOptional = getRoleById(id);
            if (!roleOptional.isPresent()) {
                throw new NoSuchElementException("Rol no encontrado con ID: " + id);
            }
            Rol role = roleOptional.get();
            if (isRoleNameOrDescriptionTaken(roleDetails.getNombre(), roleDetails.getDescripcion(), id)) {
                throw new IllegalArgumentException("El nombre o descripción del rol ya está en uso");
            }
            role.setNombre(roleDetails.getNombre());
            role.setDescripcion(roleDetails.getDescripcion());
            entityManager.merge(role);
            logger.info("Rol actualizado: {}", role.getNombre());
            return role;
        } catch (Exception e) {
            logger.error("Error al actualizar rol con ID: {}", id, e);
            throw new RuntimeException("Error al actualizar rol: " + e.getMessage());
        }
    }

    // Delete a role
    @Transactional
    public void deleteRole(Integer id) {
        try {
            if (id.equals(OSI_ROLE_ID)) {
                throw new IllegalStateException("No se puede eliminar el rol OSI");
            }
            Optional<Rol> roleOptional = getRoleById(id);
            if (!roleOptional.isPresent()) {
                throw new NoSuchElementException("Rol no encontrado con ID: " + id);
            }
            Rol role = roleOptional.get();
            entityManager.createQuery("DELETE FROM RolesFuncionalidades rf WHERE rf.id.idRol = :roleId")
                    .setParameter("roleId", id)
                    .executeUpdate();
            entityManager.remove(role);
            logger.info("Rol eliminado: {}", role.getNombre());
        } catch (Exception e) {
            logger.error("Error al eliminar rol con ID: {}", id, e);
            throw new RuntimeException("Error al eliminar rol: " + e.getMessage());
        }
    }

    // Create a new functionality
    @Transactional
    public Funcionalidad createFunctionality(Funcionalidad funcionalidad) {
        try {
            if (isFunctionalityNameOrPathTaken(funcionalidad.getNombre(), funcionalidad.getDireccion(), null)) {
                throw new IllegalArgumentException("El nombre o dirección de la funcionalidad ya está en uso");
            }
            entityManager.persist(funcionalidad);
            logger.info("Funcionalidad creada: {}", funcionalidad.getNombre());
            return funcionalidad;
        } catch (Exception e) {
            logger.error("Error al crear funcionalidad: {}", funcionalidad.getNombre(), e);
            throw new RuntimeException("Error al crear funcionalidad: " + e.getMessage());
        }
    }

    // Update an existing functionality
    @Transactional
    public Funcionalidad updateFunctionality(Integer id, Funcionalidad funcionalidadDetails) {
        try {
            if (isProtectedFunctionality(id)) {
                throw new IllegalStateException("No se puede modificar una funcionalidad protegida");
            }
            Optional<Funcionalidad> funcOptional = Optional.ofNullable(entityManager.find(Funcionalidad.class, id));
            if (!funcOptional.isPresent()) {
                throw new NoSuchElementException("Funcionalidad no encontrada con ID: " + id);
            }
            Funcionalidad funcionalidad = funcOptional.get();
            if (isFunctionalityNameOrPathTaken(funcionalidadDetails.getNombre(), funcionalidadDetails.getDireccion(), id)) {
                throw new IllegalArgumentException("El nombre o dirección de la funcionalidad ya está en uso");
            }
            funcionalidad.setNombre(funcionalidadDetails.getNombre());
            funcionalidad.setDescripcion(funcionalidadDetails.getDescripcion());
            funcionalidad.setDireccion(funcionalidadDetails.getDireccion());
            entityManager.merge(funcionalidad);
            logger.info("Funcionalidad actualizada: {}", funcionalidad.getNombre());
            return funcionalidad;
        } catch (Exception e) {
            logger.error("Error al actualizar funcionalidad con ID: {}", id, e);
            throw new RuntimeException("Error al actualizar funcionalidad: " + e.getMessage());
        }
    }

    // Delete a functionality
    @Transactional
    public void deleteFunctionality(Integer id) {
        try {
            if (isProtectedFunctionality(id)) {
                throw new IllegalStateException("No se puede eliminar una funcionalidad protegida");
            }
            Optional<Funcionalidad> funcOptional = Optional.ofNullable(entityManager.find(Funcionalidad.class, id));
            if (!funcOptional.isPresent()) {
                throw new NoSuchElementException("Funcionalidad no encontrada con ID: " + id);
            }
            Funcionalidad funcionalidad = funcOptional.get();
            entityManager.createQuery("DELETE FROM RolesFuncionalidades rf WHERE rf.id.idFuncionalidad = :funcId")
                    .setParameter("funcId", id)
                    .executeUpdate();
            entityManager.remove(funcionalidad);
            logger.info("Funcionalidad eliminada: {}", funcionalidad.getNombre());
        } catch (Exception e) {
            logger.error("Error al eliminar funcionalidad con ID: {}", id, e);
            throw new RuntimeException("Error al eliminar funcionalidad: " + e.getMessage());
        }
    }

    // Assign functionality to role (updated to remove description parameter)
    @Transactional
    public RolesFuncionalidades assignFunctionalityToRole(Integer roleId, Integer functionalityId) {
        try {
            // Verificar si la combinación ya existe
            List<RolesFuncionalidades> existingAssignments = entityManager.createQuery(
                            "SELECT rf FROM RolesFuncionalidades rf WHERE rf.id.idRol = :roleId AND rf.id.idFuncionalidad = :funcId",
                            RolesFuncionalidades.class)
                    .setParameter("roleId", roleId)
                    .setParameter("funcId", functionalityId)
                    .getResultList();
            if (!existingAssignments.isEmpty()) {
                throw new IllegalStateException("La funcionalidad ya está asignada a este rol");
            }

            // Crear nueva entidad RolesFuncionalidades
            RolesFuncionalidades rf = new RolesFuncionalidades();
            rf.setId(new RolesFuncionalidadesId(roleId, functionalityId));

            // Log para depurar los valores antes de persistir
            logger.debug("Persisting RolesFuncionalidades: id_rol={}, id_funcionalidad={}",
                    roleId, functionalityId);

            // Persistir la entidad y forzar escritura para detectar errores
            entityManager.persist(rf);
            entityManager.flush(); // Forzar la escritura para capturar errores de inmediato

            logger.info("Funcionalidad con ID {} asignada al rol con ID {}", functionalityId, roleId);
            return rf;
        } catch (Exception e) {
            logger.error("Error al asignar funcionalidad {} al rol {}: {}", functionalityId, roleId, e.getMessage(), e);
            throw new RuntimeException("Error al asignar funcionalidad: " + e.getMessage());
        }
    }

    // Helper method to check if role name or description is taken
    private boolean isRoleNameOrDescriptionTaken(String nombre, String descripcion, Integer excludeId) {
        try {
            String query = "SELECT r FROM Rol r WHERE (r.nombre = :nombre OR r.descripcion = :descripcion)";
            if (excludeId != null) {
                query += " AND r.idRol != :excludeId";
            }
            var typedQuery = entityManager.createQuery(query, Rol.class)
                    .setParameter("nombre", nombre)
                    .setParameter("descripcion", descripcion);
            if (excludeId != null) {
                typedQuery.setParameter("excludeId", excludeId);
            }
            return !typedQuery.getResultList().isEmpty();
        } catch (Exception e) {
            logger.error("Error al verificar duplicados de rol", e);
            return false;
        }
    }

    // Helper method to check if functionality name or path is taken
    private boolean isFunctionalityNameOrPathTaken(String nombre, String direccion, Integer excludeId) {
        try {
            String query = "SELECT f FROM Funcionalidad f WHERE (f.nombre = :nombre OR f.direccion = :direccion)";
            if (excludeId != null) {
                query += " AND f.idFuncionalidad != :excludeId";
            }
            var typedQuery = entityManager.createQuery(query, Funcionalidad.class)
                    .setParameter("nombre", nombre)
                    .setParameter("direccion", direccion);
            if (excludeId != null) {
                typedQuery.setParameter("excludeId", excludeId);
            }
            return !typedQuery.getResultList().isEmpty();
        } catch (Exception e) {
            logger.error("Error al verificar duplicados de funcionalidad", e);
            return false;
        }
    }

    // Helper method to check if a functionality is protected
    private boolean isProtectedFunctionality(Integer id) {
        try {
            Funcionalidad funcionalidad = entityManager.find(Funcionalidad.class, id);
            if (funcionalidad == null) {
                return false;
            }
            String path = funcionalidad.getDireccion();
            return DEFAULT_FUNCTIONALITIES.stream().anyMatch(f -> f.get("path").equals(path));
        } catch (Exception e) {
            logger.error("Error al verificar si la funcionalidad es protegida con ID: {}", id, e);
            return false;
        }
    }
}