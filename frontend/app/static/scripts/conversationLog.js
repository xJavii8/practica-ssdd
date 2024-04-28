document.addEventListener("DOMContentLoaded", function() {
    document.getElementById('delConversation').addEventListener('click', function() {
        document.getElementById('confirmationDialog').style.display = 'flex'; // Diálogo de confirmación para eliminar la conver
    });

    // Eliminación de conversación
    document.getElementById('confirmDel').addEventListener('click', function() {
        const dialogContainer = document.getElementById('confirmationDialog');
        const convID = dialogContainer.getAttribute('data-convID');

        // Solicitud backend
        fetch('/delConv', {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                convID: convID
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Algo salió mal al eliminar la conversación.');
            }
            return response.json();
        })
        .then(data => {
            window.location.href = '/allConversations';
        })
        .catch(error => {
            console.error('Error:', error);
        });

        document.getElementById('confirmationDialog').style.display = 'none';
    });

    // Cancelación de eliminación
    document.getElementById('cancelDel').addEventListener('click', function() {
        document.getElementById('confirmationDialog').style.display = 'none';
    });
    
});

function addMessage(sender, text) {
    const chatArea = document.getElementById('chatArea');
    const messageDiv = document.createElement('div');
    messageDiv.classList.add('message', `${sender}-message`);

    const imgDiv = document.createElement('div');
    imgDiv.classList.add('image');
    imgDiv.style.backgroundImage = sender === 'user' ? "url('/static/images/userIcon.png')" : "url('/static/images/mainIcon.webp')";

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

function loadMessages(dialogues) {
    // Cargamos los mensajes de la conversación
    dialogues.forEach(dialogue => {
        addMessage('user', dialogue.prompt);
        addMessage('model', dialogue.answer);
    });
}