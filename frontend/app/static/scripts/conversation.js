document.addEventListener("DOMContentLoaded", function() {
    const chatForm = document.getElementById('chatForm');
    const userInput = document.getElementById('userInput');
    const chatArea = document.getElementById('chatArea');

    chatForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const userText = userInput.value;
        if (userText.trim() !== '') {
            addMessage('user', userText);

            // Test respuesta modelo
            setTimeout(() => {
                const modelResponse = "Esta es una respuesta simulada del modelo.";
                addMessage('model', modelResponse);
            }, 500);

            userInput.value = '';
            userInput.focus();
        }
    });

    document.getElementById('endConversation').addEventListener('click', function() {
        document.getElementById('confirmationDialog').style.display = 'flex'; // Diálogo de confirmación para terminar la conver
    });

    // Finalización de conversación
    document.getElementById('confirmEnd').addEventListener('click', function() {
        const dialogContainer = document.getElementById('confirmationDialog');
        const conversationName = dialogContainer.getAttribute('data-convName');

        // Solicitud backend
        fetch('/endConv', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                convName: conversationName
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Algo salió mal al finalizar la conversación.');
            }
            return response.json();
        })
        .then(data => {
            console.log(data.message);
            window.location.href = '/';
        })
        .catch(error => {
            console.error('Error:', error);
        });

        document.getElementById('confirmationDialog').style.display = 'none';
    });

    // Cancelación de finalización
    document.getElementById('cancelEnd').addEventListener('click', function() {
        document.getElementById('confirmationDialog').style.display = 'none';
    });

    function addMessage(sender, text) {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add('message', `${sender}-message`);
    
        const imgDiv = document.createElement('div');
        imgDiv.classList.add('image');
        imgDiv.style.backgroundImage = sender === 'user' ? "url('/static/images/userIcon.png')" : "url('/static/mainIcon.webp')";
    
        const textDiv = document.createElement('div');
        textDiv.classList.add('text');
        textDiv.textContent = text;
    
        if (sender === 'model') {
            messageDiv.appendChild(imgDiv);
            messageDiv.appendChild(textDiv);
        } else {
            messageDiv.appendChild(textDiv);
            messageDiv.appendChild(imgDiv);
    
            // Cambiar más adelante
            fetch('http://backend-rest:8080/Service/', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ message: text })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error'); // Placeholder
                }
                return response.json();
            })
            .then(data => {
                console.log("Mensaje enviado con éxito:", data);
            })
            .catch(error => {
                console.error("Error al enviar el mensaje:", error);
            });
        }
    
        chatArea.appendChild(messageDiv);
        chatArea.scrollTop = chatArea.scrollHeight;
    }
    
});