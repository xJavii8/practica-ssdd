document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll('.conversation-name').forEach(item => {
        item.addEventListener('click', () => {
            const convID = item.getAttribute('data-convID');
            fetch('/getConvData', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ convID: convID })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Algo salió mal');
                }
                return response.json();
            })
            .then(data => {
                window.location.href = '/conversation';
            })
            .catch(error => {
                console.error('Error:', error);
            });
        });
    });
});
