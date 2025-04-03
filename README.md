# MengBooru

## Backend (Spring Boot)

Le backend de MengBooru est une application Spring Boot utilisant H2 comme base de données et est structuré selon mes meilleures pratiques ;

### Config :
- Support de H2 avec console web
- Stockage hybride: fichiers en base de données pour les petits médias, système de fichiers pour les gros

### API REST :
- Endpoints pour uploader, récupérer et rechercher des médias
- Support pour les collections et les tags

### Interface web :
- Template Thymeleaf pour afficher les médias sous forme de grille
- Filtrage par collection et tags

## Extension Firefox

L'extension Firefox est conçue pour capturer des médias depuis des sites web ;

### Menu contextuel :
- Clic droit sur une image pour la sauvegarder dans MengBooru

### Popup d'upload :
- Prévisualisation du média
- Formulaire pour nommer le fichier, ajouter des tags et sélectionner une collection
- Option pour créer une nouvelle collection

### Options :
- Configuration de l'URL du serveur backend

## Instructions pour le déploiement

### Backend:
```bash
# Compilation avec
./gradlew build
# Launch avec
java -jar build/libs/mengBooru.jar
```
- Accède à l'interface à http://localhost:8080 (ou via le dashboard)
- Console H2 disponible à http://localhost:8080/h2-console

### Extension Firefox:
1. Firefox > about:debugging
2. Cliquer sur "Ce Firefox" puis "Charger un module temporaire"
3. Sélectionner le `manifest.json` de l'extension firefox
4. Configurer l'URL du serveur dans les options si nécessaire
