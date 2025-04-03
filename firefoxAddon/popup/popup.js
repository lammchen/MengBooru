// Variables globales
let currentMediaUrl = null;
let currentMediaBlob = null;
let serverUrl = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', async () => {
    try {
      // Demander l'URL du média au background script
      browser.runtime.sendMessage({ action: "getSelectedMedia" })
        .then(response => {
          if (response && response.mediaUrl) {
            // Afficher le média dans le popup
            setupMediaPreview(response.mediaUrl);
          } else {
            // Si aucun média n'est sélectionné, afficher un message
            document.getElementById('media-preview').innerHTML = '<p>Aucun média sélectionné. Utilisez le menu contextuel sur une image.</p>';
          }
        });
      
      // Charger les collections disponibles
      loadCollections();
      
      // Configuration du formulaire
      setupForm();

      // Configuration du bouton dashboard
      document.getElementById('dashboard-button').addEventListener('click', function() {
        browser.runtime.sendMessage({ action: "openDashboard" });
        window.close(); // Fermer le popup après avoir ouvert le dashboard
      });
      
    } catch (error) {
      console.error('Erreur lors de l\'initialisation du popup:', error);
      showStatus('Erreur d\'initialisation', 'error');
    }
  });

// Au chargement du popup
document.addEventListener('DOMContentLoaded', async () => {
  try {
    // Récupérer l'onglet actif
    const tabs = await browser.tabs.query({ active: true, currentWindow: true });
    const activeTab = tabs[0];
    
    // Récupération des médias sélectionnés ou disponibles sur la page
    browser.tabs.sendMessage(activeTab.id, { action: 'getSelectedMedia' }, (response) => {
      if (response && response.mediaUrl) {
        // Afficher le média dans le popup
        setupMediaPreview(response.mediaUrl);
      } else {
        // Si aucun média n'est sélectionné, afficher un message
        document.getElementById('media-preview').innerHTML = '<p>Aucun média sélectionné. Utilisez le menu contextuel sur une image.</p>';
      }
    });
    
    // Charger les collections disponibles
    loadCollections();
    
    // Configuration du formulaire
    setupForm();
    
  } catch (error) {
    console.error('Erreur lors de l\'initialisation du popup:', error);
    showStatus('Erreur d\'initialisation', 'error');
  }
});

// Configurer la prévisualisation du média
function setupMediaPreview(mediaUrl) {
  currentMediaUrl = mediaUrl;
  const mediaImage = document.getElementById('media-image');
  
  // Afficher l'image
  mediaImage.src = mediaUrl;
  
  // Récupérer le blob du média pour l'upload
  fetch(mediaUrl)
    .then(response => response.blob())
    .then(blob => {
      currentMediaBlob = blob;
      
      // Extraire le nom de fichier de l'URL et le suggérer comme nom par défaut
      const urlParts = mediaUrl.split('/');
      const fileName = urlParts[urlParts.length - 1].split('?')[0];
      
      document.getElementById('name').value = fileName;
    })
    .catch(error => {
      console.error('Erreur lors de la récupération du média:', error);
      showStatus('Erreur lors de la récupération du média', 'error');
    });
}

// Charger les collections depuis le serveur
function loadCollections() {
  fetch(`${serverUrl}/api/media/collections`)
    .then(response => response.json())
    .then(collections => {
      const collectionSelect = document.getElementById('collection');
      
      // Conserver l'option par défaut
      const defaultOption = collectionSelect.options[0];
      collectionSelect.innerHTML = '';
      collectionSelect.appendChild(defaultOption);
      
      // Ajouter les collections depuis le serveur
      collections.forEach(collection => {
        if (collection !== 'board') { // Éviter le doublon avec l'option par défaut
          const option = document.createElement('option');
          option.value = collection;
          option.textContent = collection;
          collectionSelect.appendChild(option);
        }
      });
      
      // Ajouter l'option "Nouvelle collection"
      const newCollectionOption = document.createElement('option');
      newCollectionOption.value = 'new';
      newCollectionOption.textContent = '+ Nouvelle collection';
      collectionSelect.appendChild(newCollectionOption);
    })
    .catch(error => {
      console.error('Erreur lors du chargement des collections:', error);
    });
}

// Configurer le comportement du formulaire
function setupForm() {
  // Afficher/masquer le champ de nouvelle collection
  const collectionSelect = document.getElementById('collection');
  const newCollectionInput = document.getElementById('new-collection');
  
  collectionSelect.addEventListener('change', function() {
    if (this.value === 'new') {
      newCollectionInput.style.display = 'block';
      newCollectionInput.focus();
    } else {
      newCollectionInput.style.display = 'none';
    }
  });
  
  // Gérer la soumission du formulaire
  document.getElementById('upload-form').addEventListener('submit', function(event) {
    event.preventDefault();
    
    if (!currentMediaBlob) {
      showStatus('Aucun média à télécharger', 'error');
      return;
    }
    
    uploadMedia();
  });
}

// Télécharger le média vers le serveur
function uploadMedia() {
  showStatus('Téléchargement en cours...', '');
  
  // Récupérer les valeurs du formulaire
  const name = document.getElementById('name').value;
  const tags = document.getElementById('tags').value;
  const collectionSelect = document.getElementById('collection');
  let collection = collectionSelect.value;
  
  // Si "Nouvelle collection" est sélectionnée, utiliser la valeur du champ de texte
  if (collection === 'new') {
    collection = document.getElementById('new-collection').value;
    if (!collection) {
      showStatus('Veuillez spécifier un nom de collection', 'error');
      return;
    }
  }
  
  // Créer un objet FormData pour l'upload multipart
  const formData = new FormData();
  
  // Générer un nom de fichier avec extension basé sur le type MIME
  const fileExtension = getExtensionFromMimeType(currentMediaBlob.type);
  const fileName = name || `media_${Date.now()}.${fileExtension}`;
  
  // Ajouter le fichier et les métadonnées au FormData
  formData.append('file', currentMediaBlob, fileName);
  if (name) formData.append('name', name);
  if (tags) formData.append('tags', tags);
  if (collection) formData.append('collection', collection);
  
  // Envoyer la requête au serveur
  fetch(`${serverUrl}/api/media`, {
    method: 'POST',
    body: formData
  })
  .then(response => {
    if (!response.ok) {
      throw new Error(`Erreur HTTP: ${response.status}`);
    }
    return response.json();
  })
  .then(data => {
    showStatus('Média sauvegardé avec succès!', 'success');
    
    // Réinitialiser le formulaire après quelques secondes
    setTimeout(() => {
      document.getElementById('upload-form').reset();
      window.close(); // Fermer le popup
    }, 2000);
  })
  .catch(error => {
    console.error('Erreur lors du téléchargement:', error);
    showStatus('Erreur lors du téléchargement', 'error');
  });
}

// Afficher un message de statut
function showStatus(message, type) {
  const statusElement = document.getElementById('status-message');
  statusElement.textContent = message;
  statusElement.className = type; // CSS class (success, error, ...)
}

// Obtenir l'extension de fichier à partir du type MIME
function getExtensionFromMimeType(mimeType) {
  const mimeToExt = {
    'image/jpeg': 'jpg',
    'image/png': 'png',
    'image/gif': 'gif',
    'image/webp': 'webp',
    'image/svg+xml': 'svg',
    'video/mp4': 'mp4',
    'video/webm': 'webm',
    'application/pdf': 'pdf'
  };
  
  return mimeToExt[mimeType] || 'bin';
}