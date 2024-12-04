using LangChain.Databases;
using LangChain.DocumentLoaders;
using LangChain.Extensions;
using LangChain.Providers;

namespace DurianNet.Utils
{
    public class VectorDbUtils
    {

        /// <summary>
        /// Load the clean text files to the vector database in <b>parallel</b>. <br />
        /// </summary>
        /// <param name="vectorDatabase"></param>
        /// <param name="embeddingModel"></param>
        /// <returns></returns>
        public static async Task LoadTextFilesToVectorDB(IVectorDatabase vectorDatabase, IEmbeddingModel embeddingModel)
        {
            Console.WriteLine("Loading Clean documents to vector db...");

            var vectorCollection = await vectorDatabase.GetOrCreateCollectionAsync("durian_clean", dimensions: 768);

            // get FOCS under Assets folder path
            string assetsPath = Path.GetFullPath("Assets/Durian");
            Console.WriteLine("Assets Path: " + assetsPath);

            //load all text files in the folder
            var files = Directory.GetFiles(assetsPath, "*.txt");

            Console.WriteLine("Loading Files: ");
            foreach (var file in files)
            {
                Console.WriteLine("File: " + file);
            }

            // Create tasks for parallel execution
            var tasks = files.Select(file =>
                vectorCollection.AddDocumentsFromAsync<FileLoader>(
                    embeddingModel,
                    dataSource: DataSource.FromPath(file))
            ).ToArray();
            try
            {
                await Task.WhenAll(tasks).ConfigureAwait(false);
            }
            catch (Exception ex)
            {
                // Handle the exception
                Console.WriteLine(ex.Message);
            }

            Console.WriteLine("Complete Loading the text files...\n");

        }
    }
}
