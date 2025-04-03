// Configuration et variables globales
let serverUrl = 'http://localhost:8080';
let mediaData = [];
let collectionsData = {};
let tagsData = {};

// Au chargement du document
document.addEventListener('DOMContentLoaded', () => {
  // Récupérer l'URL du serveur depuis les options
  browser.storage.local.get('serverUrl', (result) => {
    if (result.serverUrl) {
      serverUrl = result.serverUrl;
    }
    
    // Initialiser le dashboard
    initDashboard();
  });
  
  // Gestionnaire d'événements pour les onglets
  setupTabs();
  
  // Configuration du bouton d'ouverture de l'interface web
  document.getElementById('open-web-interface').addEventListener('click', () => {
    browser.tabs.create({ url: serverUrl });
  });
  
  // Configuration du formulaire de recherche
  document.getElementById('search-button').addEventListener('click', performSearch);
});

// Initialisation du dashboard
async function initDashboard() {
  try {
    // Vérifier la connexion au serveur
    await checkServerConnection();
    
    // Charger les données de base
    await Promise.all([
      loadRecentMedia(),
      loadCollections(),
      loadTags(),
      loadStats()
    ]);
    
  } catch (error) {
    console.error('Erreur lors de l\'initialisation du dashboard:', error);
    setConnectionStatus(false);
  }
}

// Vérifier la connexion au serveur
async function checkServerConnection() {
  try {
    const response = await fetch(`${serverUrl}/api/media/collections`);
    if (response.ok) {
      setConnectionStatus(true);
      return true;
    } else {
      setConnectionStatus(false);
      return false;
    }
  } catch (error) {
    setConnectionStatus(false);
    throw new Error('Impossible de se connecter au serveur MengBooru');
  }
}

// Mettre à jour le statut de connexion
function setConnectionStatus(isConnected) {
  const statusElement = document.getElementById('connection-status');
  if (isConnected) {
    statusElement.textContent = 'Connecté au serveur';
    statusElement.classList.add('connected');
    statusElement.classList.remove('disconnected');
  } else {
    statusElement.textContent = 'Déconnecté';
    statusElement.classList.add('disconnected');
    statusElement.classList.remove('connected');
  }
}

// Chargement des médias récents
async function loadRecentMedia() {
  try {
    const response = await fetch(`${serverUrl}/api/media`);
    if (!response.ok) throw new Error('Erreur serveur');
    
    mediaData = await response.json();
    
    const container = document.getElementById('recent-media-container');
    
    if (mediaData.length === 0) {
      container.innerHTML = '<div class="empty-state">Aucun média trouvé</div>';
      return;
    }
    
    // Afficher les 12 médias les plus récents
    container.innerHTML = '';
    mediaData.slice(0, 12).forEach(media => {
      container.appendChild(createMediaElement(media));
    });
    
  } catch (error) {
    console.error('Erreur lors du chargement des médias récents:', error);
    document.getElementById('recent-media-container').innerHTML = 
      '<div class="empty-state">Erreur lors du chargement des médias</div>';
  }
}

// Chargement des collections
async function loadCollections() {
  try {
    const [collectionsResponse, collectionsWithStatsResponse] = await Promise.all([
      fetch(`${serverUrl}/api/media/collections`),
      fetch(`${serverUrl}/collections`) // Endpoint spécifique qui retourne les stats
    ]);
    
    if (!collectionsResponse.ok) throw new Error('Erreur serveur - collections');
    
    const collections = await collectionsResponse.json();
    
    // Remplir le sélecteur de collection pour la recherche
    const selectElement = document.getElementById('search-collection');
    selectElement.innerHTML = '<option value="">Toutes les collections</option>';
    
    collections.forEach(collection => {
      const option = document.createElement('option');
      option.value = collection;
      option.textContent = collection;
      selectElement.appendChild(option);
    });
    
    // Si les données avec statistiques sont disponibles
    if (collectionsWithStatsResponse.ok) {
      const stats = await collectionsWithStatsResponse.json();
      collectionsData = stats;
      
      // Afficher les collections dans l'onglet collections
      const container = document.getElementById('collections-container');
      
      if (Object.keys(stats).length === 0) {
        container.innerHTML = '<div class="empty-state">Aucune collection trouvée</div>';
        return;
      }
      
      container.innerHTML = '';
      Object.entries(stats).forEach(([name, data]) => {
        container.appendChild(createCollectionElement(name, data));
      });
    }
    
  } catch (error) {
    console.error('Erreur lors du chargement des collections:', error);
    document.getElementById('collections-container').innerHTML = 
      '<div class="empty-state">Erreur lors du chargement des collections</div>';
  }
}

// Chargement des tags
async function loadTags() {
  try {
    const [tagsResponse, tagsWithStatsResponse] = await Promise.all([
      fetch(`${serverUrl}/api/media/tags`),
      fetch(`${serverUrl}/tags`) // Endpoint spécifique qui retourne les stats
    ]);
    
    if (!tagsResponse.ok) throw new Error('Erreur serveur - tags');
    
    // Si les données avec statistiques sont disponibles
    if (tagsWithStatsResponse.ok) {
      const stats = await tagsWithStatsResponse.json();
      tagsData = stats;
      
      // Afficher les tags dans l'onglet tags
      const container = document.getElementById('tags-container');
      
      if (Object.keys(stats).length === 0) {
        container.innerHTML = '<div class="empty-state">Aucun tag trouvé</div>';
        return;
      }
      
      container.innerHTML = '';
      Object.entries(stats).forEach(([name, data]) => {
        container.appendChild(createTagElement(name, data));
      });
    }
    
  } catch (error) {
    console.error('Erreur lors du chargement des tags:', error);
    document.getElementById('tags-container').innerHTML = 
      '<div class="empty-state">Erreur lors du chargement des tags</div>';
  }
}

// Chargement des statistiques générales
async function loadStats() {
  try {
    // Compter les éléments déjà chargés
    document.getElementById('media-count').textContent = 
      `${mediaData.length} média${mediaData.length > 1 ? 's' : ''}`;
    
    document.getElementById('collection-count').textContent = 
      `${Object.keys(collectionsData).length} collection${Object.keys(collectionsData).length > 1 ? 's' : ''}`;
    
    document.getElementById('tag-count').textContent = 
      `${Object.keys(tagsData).length} tag${Object.keys(tagsData).length > 1 ? 's' : ''}`;
    
  } catch (error) {
    console.error('Erreur lors du chargement des statistiques:', error);
  }
}

// Créer un élément média
function createMediaElement(media) {
  const mediaElement = document.createElement('div');
  mediaElement.className = 'media-item';
  mediaElement.setAttribute('data-id', media.id);
  
  const isImage = media.contentType && media.contentType.startsWith('image/');
  
  mediaElement.innerHTML = `
    <div class="media-preview">
      ${isImage ? 
        `<img src="${serverUrl}/api/media/${media.id}" alt="${media.name}">` : 
        `<div class="non-image">${media.contentType || 'Type inconnu'}</div>`}
    </div>
    <div class="media-info">
      <div class="media-name">${media.name}</div>
      <div class="media-tags">
        ${media.tags && media.tags.length > 0 ? 
          media.tags.slice(0, 3).map(tag => `<span class="tag">${tag}</span>`).join('') : 
          ''}
      </div>
    </div>
  `;
  
  mediaElement.addEventListener('click', () => {
    browser.tabs.create({ url: `${serverUrl}/media/${media.id}` });
  });
  
  return mediaElement;
}

// Créer un élément collection
function createCollectionElement(name, data) {
  const collectionElement = document.createElement('div');
  collectionElement.className = 'collection-item';
  
  const hasPreviewMedia = data.previewMedia !== undefined;
  
  collectionElement.innerHTML = `
    <div class="collection-preview">
      ${hasPreviewMedia ? 
        `<img src="${serverUrl}/api/media/${data.previewMedia.id}" alt="${name}">` : 
        `<div class="no-preview">Pas d'aperçu</div>`}
    </div>
    <div class="collection-info">
      <div class="collection-name">${name}</div>
      <div class="collection-count">${data.count} média${data.count > 1 ? 's' : ''}</div>
    </div>
  `;
  
  collectionElement.addEventListener('click', () => {
    browser.tabs.create({ url: `${serverUrl}/?collection=${encodeURIComponent(name)}` });
  });
  
  return collectionElement;
}

// Créer un élément tag
function createTagElement(name, data) {
  const tagElement = document.createElement('div');
  tagElement.className = 'tag-item';
  
  const hasPreviewMedia = data.previewMedia !== undefined;
  
  tagElement.innerHTML = `
    <div class="tag-preview">
      ${hasPreviewMedia ? 
        `<img src="${serverUrl}/api/media/${data.previewMedia.id}" alt="${name}">` : 
        `<div class="no-preview"></div>`}
    </div>
    <div class="tag-info">
      <span class="tag-name">${name}</span>
      <span class="tag-count">(${data.count})</span>
    </div>
  `;
  
  tagElement.addEventListener('click', () => {
    browser.tabs.create({ url: `${serverUrl}/?tag=${encodeURIComponent(name)}` });
  });
  
  return tagElement;
}

// Configurer les onglets
function setupTabs() {
  const tabs = document.querySelectorAll('.nav-item');
  
  tabs.forEach(tab => {
    tab.addEventListener('click', () => {
      // Enlever la classe active de tous les onglets
      document.querySelectorAll('.nav-item').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
      
      // Ajouter la classe active à l'onglet cliqué
      tab.classList.add('active');
      
      // Afficher le contenu correspondant
      const tabId = tab.getAttribute('data-tab');
      document.getElementById(tabId).classList.add('active');
    });
  });
}

// Effectuer une recherche
async function performSearch() {
  const tagsInput = document.getElementById('search-tags').value;
  const collectionInput = document.getElementById('search-collection').value;
  
  if (!tagsInput && !collectionInput) {
    alert('Veuillez entrer au moins un critère de recherche');
    return;
  }
  
  try {
    const resultsContainer = document.getElementById('search-results');
    resultsContainer.innerHTML = '<div class="loading">Recherche en cours...</div>';
    
    // Construire l'URL de recherche
    let searchUrl = `${serverUrl}/api/media?`;
    if (tagsInput) {
      // Si plusieurs tags sont entrés, nous utilisons le premier pour l'API
      const firstTag = tagsInput.split(',')[0].trim();
      searchUrl += `tag=${encodeURIComponent(firstTag)}`;
    }
    
    if (collectionInput) {
      if (tagsInput) searchUrl += '&';
      searchUrl += `collection=${encodeURIComponent(collectionInput)}`;
    }
    
    const response = await fetch(searchUrl);
    if (!response.ok) throw new Error('Erreur lors de la recherche');
    
    const results = await response.json();
    
    if (results.length === 0) {
      resultsContainer.innerHTML = '<div class="empty-state">Aucun résultat trouvé</div>';
      return;
    }
    
    resultsContainer.innerHTML = '';
    results.forEach(media => {
      resultsContainer.appendChild(createMediaElement(media));
    });
    
  } catch (error) {
    console.error('Erreur lors de la recherche:', error);
    document.getElementById('search-results').innerHTML = 
      '<div class="empty-state">Erreur lors de la recherche</div>';
  }
}