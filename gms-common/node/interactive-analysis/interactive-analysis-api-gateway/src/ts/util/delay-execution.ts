export const delayExecution = async <T> (func: () => T, delayMillis: number = 50) => (
    new Promise<T>((resolve, reject) => {
        setTimeout(
            () => {
                try {
                    resolve(func());
                } catch (e) {
                    reject(e);
                }
            },
            delayMillis);
    })
);
