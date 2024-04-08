document.addEventListener("DOMContentLoaded", function() {
    const chatForm = document.getElementById('chatForm');
    const userInput = document.getElementById('userInput');
    const chatArea = document.getElementById('chatArea');
    const button = document.getElementById('button-addon2');

    button.disabled = true;

    userInput.addEventListener('input', function() {
        // Si hay texto se habilita el botón, de lo contrario, se deshabilita
        button.disabled = !this.value.length;
    });

    chatForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const userText = userInput.value.trim();
        if (userText !== '') {
            addMessage('user', userText);

            fetch('/sendPrompt', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ prompt: userText })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al enviar el mensaje');
                }
                return response.json();
            })
            .then(data => {
                addMessage('model', data.modelResponse);
            })
            .catch(error => {
                console.error("Error:", error);
            });

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
        const convID = dialogContainer.getAttribute('data-convID');

        // Solicitud backend
        fetch('/endConv', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                convID: convID
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
        }
    
        chatArea.appendChild(messageDiv);
        chatArea.scrollTop = chatArea.scrollHeight;
    }
    
});