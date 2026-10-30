# AtlasRH — backlog de livraison

Ce document est structuré pour reporter les tickets dans le projet Jira de l’équipe. Les noms de clés Jira restent à attribuer lors de l’import.

## EPIC RH — référentiel des collaborateurs

### US1 — Gérer les départements

**Critères d’acceptation**

- Le Responsable RH peut créer, consulter, modifier et supprimer un département.
- Le nom est unique ; un conflit renvoie `409`.
- Un département contenant des employés ne peut pas être supprimé (`409`).
- Les entrées invalides renvoient `400` et les IDs inconnus `404`.

**Tâches** : entité et migration Liquibase ; repository ; service avec unicité et règle de suppression ; DTO/MapStruct ; contrôleur documenté ; scénario Postman.

### US2 — Gérer les employés

**Critères d’acceptation**

- Le matricule et l’e-mail sont uniques ; un employé ne peut être son propre manager.
- La fiche référence un département et peut référencer un manager.
- La liste supporte recherche, filtre département, tri et pagination ; l’équipe d’un manager est consultable.
- Les erreurs de validation, unicité et références inconnues utilisent les statuts HTTP adaptés.

**Tâches** : modèle et contraintes ; DTO/MapStruct ; repository de recherche paginée ; service de règles ; routes CRUD et équipe ; tests de service et d’intégration.

### US3 — Gérer les contrats

**Critères d’acceptation**

- Un contrat CDI, CDD ou STAGE peut être créé et consulté dans l’historique d’un employé.
- CDD et STAGE exigent une date de fin ; fin avant début rejetée.
- Un employé ne peut avoir qu’un contrat en cours ; un contrat en cours peut être terminé une fois.

**Tâches** : entité/migration/contraintes ; validation métier ; DTO/MapStruct ; endpoints de création, historique et fin ; scénarios Postman.

## EPIC Congés — libre-service et validation hiérarchique

### US4 — Valider les demandes de mon équipe

**Critères d’acceptation**

- Un manager ne voit que les demandes en attente des employés qui lui sont rattachés.
- Il peut approuver ou refuser une demande en attente ; le refus exige un commentaire.
- Une demande déjà décidée ne peut pas être décidée une seconde fois.

**Tâches** : requête manager/demandes ; DTO de décision ; règles de transition et de commentaire ; endpoints documentés ; tests.

### US5 — Faire et suivre une demande de congé

**Critères d’acceptation**

- L’employé crée une demande avec dates, type et motif ; nombre de jours calculé sur les jours ouvrés inclusifs.
- Les dates inversées, périodes qui se chevauchent et demandes dépassant le quota sont rejetées.
- L’historique est filtrable par statut ; seule une demande en attente peut être annulée par son employé.

**Tâches** : types et quotas de démonstration ; calcul des jours ouvrés ; règles de recouvrement et quota ; annulation/filtre ; tests de service et de bout en bout.

### US6 — Consulter solde et ancienneté

**Critères d’acceptation**

- Le solde restant est dérivé du quota annuel moins les jours approuvés dans l’année demandée.
- L’ancienneté est calculée depuis la date d’embauche en années, mois et jours.
- Les IDs inconnus renvoient `404`.

**Tâches** : requêtes de calcul ; DTO de solde et ancienneté ; endpoints ; tests des changements d’année et de dates.

## EPIC Livraison — qualité et démonstration

**Tâches** : OpenAPI avec résumé par endpoint ; README et exemples Postman ; diagramme de classes ; vérification des migrations PostgreSQL ; jeu de données fictives ; répétition de la présentation de 15 minutes.
