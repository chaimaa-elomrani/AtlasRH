# Démonstration AtlasRH — 15 minutes

Préparer PostgreSQL avec `docker compose up -d postgres`, lancer l’API avec `mvn spring-boot:run`, puis ouvrir `http://localhost:8080/swagger-ui/index.html`. Les données fictives incluent un responsable RH, un employé rattaché et son contrat ; récupérer leurs IDs avec `GET /api/employees` avant la démonstration.

| Temps | Démonstration |
| --- | --- |
| 0:00–1:30 | Présenter le problème métier et les trois profils sans authentification en V1. |
| 1:30–3:00 | Expliquer le découpage controller, service, repository, entity, DTO et mapper. Montrer OpenAPI et PostgreSQL/Liquibase. |
| 3:00–5:00 | Créer un département, puis un employé ; montrer validation et unicité. |
| 5:00–6:00 | Lister les employés avec pagination/tri et rechercher par département. Afficher l’équipe du manager. |
| 6:00–7:00 | Créer un contrat et consulter l’historique. |
| 7:00–9:00 | Depuis l’employé, créer une demande ; montrer le calcul des jours ouvrés et le statut en attente. |
| 9:00–10:30 | Depuis le manager, lister les demandes de l’équipe et approuver une demande. Montrer le commentaire de refus obligatoire avec une seconde demande si le temps le permet. |
| 10:30–12:00 | Reconsulter l’historique et le solde ; montrer qu’une demande en attente peut être annulée. |
| 12:00–13:00 | Montrer ancienneté et erreurs métier pertinentes (`400`, `404`, `409`). |
| 13:00–15:00 | Résumer les limites V1 et répondre aux questions. |

## Préparation

- Vérifier `GET /api/leave-types` et les IDs des employés de démonstration.
- Utiliser des dates futures en jours ouvrés, sans chevauchement avec des demandes existantes.
- Préparer une demande distincte pour illustrer le refus avec commentaire.
- Garder la collection Postman avec les IDs remplaçables en variables (`baseUrl`, `employeeId`, `managerId`, `leaveTypeId`).

## Limites à signaler

- Aucun mécanisme d’authentification/autorisation ; les IDs d’URL identifient seulement les profils simulés.
- Les jours fériés ne sont pas exclus du calcul des jours ouvrés.
- Les notifications e-mail, l’organigramme et l’audit sont des bonus hors périmètre livré.
