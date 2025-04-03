// Chargement des options sauvegardées
document.addEventListener('DOMContentLoaded', () => {
    browser.storage.local.get('serverUrl', (result) => {
      if (result.serverUrl) {
        document.getElementById('server-url').value = result.serverUrl;
      }
    });
    
    document.getElementById('options-form').addEventListener('submit', (event) => {
      event.preventDefault();
      
      const serverUrl = document.getElementById('server-url').value.trim();
      
      if (!serverUrl) {
        showStatus('Veuillez entrer une URL de serveur valide', 'error');
        return;
      }
      
      browser.storage.local.set({ serverUrl }, () => {
        showStatus('Options sauvegardées avec succès', 'success');
        
        testServerConnection(serverUrl);
      });
    });
  });
  
  // Afficher le message de statut
  function showStatus(message, type) {
    const statusElement = document.getElementById('status');
    statusElement.textContent = message;
    statusElement.className = `status ${type}`;
    statusElement.style.display = 'block';
    
    setTimeout(() => {
      statusElement.style.display = 'none';
    }, 3000); //timeout de co
  }
  
  // Tester la connexion au serveur
  function testServerConnection(serverUrl) {
    fetch(`${serverUrl}/api/media/collections`)
      .then(response => {
        if (response.ok) {
          showStatus('Connexion au serveur réussie', 'success');
        } else {
          showStatus(`Erreur de connexion au serveur: ${response.status}`, 'error');
        }
      })
      .catch(error => {
        showStatus(`Impossible de se connecter au serveur: ${error.message}`, 'error');
      });
  }