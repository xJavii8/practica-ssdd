document.addEventListener("DOMContentLoaded", function() {
    const chatForm = document.getElementById('chatForm');
    const userInput = document.getElementById('userInput');
    const button = document.getElementById('button-addon2');

    button.disabled = true;

    userInput.addEventListener('input', function() {
        // Si hay texto se habilita el botón, de lo contrario, se deshabilita
        button.disabled = !this.value.length;
    });

    // Procesamiento de envío de prompt
    chatForm.addEventListener('submit', function(e) {
        e.preventDefault();
        const userText = userInput.value.trim();
        if (userText !== '') {
            addMessage('user', userText);
            addMessage('model', 'loading');

            fetch('/sendPrompt', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ prompt: userText })
            })
            .then(response => {
                const existingLoadingMessage = document.getElementById('loadingMessage');
                if(existingLoadingMessage) {
                    existingLoadingMessage.remove();
                }

                if(response.status === 204) {
                    console.log("No se puede mandar un mensaje mientras está en BUSY o FINISHED");
                    return {status: 204};
                }

                if (!response.ok) {
                    throw new Error('Error al enviar el mensaje');
                }
                return response.json();
            })
            .then(data => {
                if(data.status === 204) {
                    window.location.reload();
                    console.log("No se puede mandar un mensaje mientras está en BUSY o FINISHED");
                } else if(data.dialogues && data.dialogues.length > 0) {
                    const lastDialogue = data.dialogues[data.dialogues.length - 1];
                    addMessage('model', lastDialogue.answer);
                }
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
            window.location.href = '/';
        })
        .catch(error => {
            const existingLoadingMessage = document.getElementById('loadingMessage');
            if(existingLoadingMessage) {
                existingLoadingMessage.remove();
            }

            console.error('Error:', error);
        });

        document.getElementById('confirmationDialog').style.display = 'none';
    });

    // Cancelación de finalización
    document.getElementById('cancelEnd').addEventListener('click', function() {
        document.getElementById('confirmationDialog').style.display = 'none';
    });
    
});

function addMessage(sender, text) {
    const chatArea = document.getElementById('chatArea');
    const messageDiv = document.createElement('div');
    messageDiv.classList.add('message', `${sender}-message`);

    const imgDiv = document.createElement('div');
    imgDiv.classList.add('image');
    imgDiv.style.backgroundImage = sender === 'user' ? "url('/static/images/userIcon.png')" : "url('/static/mainIcon.webp')";

    const textDiv = document.createElement('div');
    textDiv.classList.add('text');

    // Mientras se está generando la respuesta, damos a entender al usuario que se está procesando su petición
    if(text === "loading" && sender === 'model') {
        textDiv.innerHTML = '<div class="spinner-grow text-secondary" role="status"><span class="visually-hidden">Loading...</span></div>';
        const existingLoadingMessage = document.getElementById('loadingMessage');
        if(existingLoadingMessage) {
            existingLoadingMessage.remove();
        }
        messageDiv.id = 'loadingMessage';
    } else {
        textDiv.textContent = text;
    }

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

function loadMessages(dialogues) {
    // Cargamos los mensajes de la conversación
    dialogues.forEach(dialogue => {
        addMessage('user', dialogue.prompt);
        addMessage('model', dialogue.answer);
    });
}