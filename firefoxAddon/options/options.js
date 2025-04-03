// Charger les options sauvegardées
document.addEventListener('DOMContentLoaded', () => {
    browser.storage.local.get('serverUrl', (result) => {
      if (result.serverUrl) {
        document.getElementById('server-url').value = result.serverUrl;
      }
    });
    
    // Gérer la soumission du formulaire
    document.getElementById('options-form').addEventListener('submit', (event) => {
      event.preventDefault();
      
      const serverUrl = document.getElementById('server-url').value.trim();
      
      // Valider l'URL
      if (!serverUrl) {
        showStatus('Veuillez entrer une URL de serveur valide', 'error');
        return;
      }
      
      // Sauvegarder les options
      browser.storage.local.set({ serverUrl }, () => {
        showStatus('Options sauvegardées avec succès', 'success');
        
        // Tester la connexion au serveur
        testServerConnection(serverUrl);
      });
    });
  });
  
  // Afficher un message de statut
  function showStatus(message, type) {
    const statusElement = document.getElementById('status');
    statusElement.textContent = message;
    statusElement.className = `status ${type}`;
    statusElement.style.display = 'block';
    
    // Masquer le message après un certain temps
    setTimeout(() => {
      statusElement.style.display = 'none';
    }, 3000);
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