# Data Fragment

The data fragment is where the data recorded in the [Home Fragment](homeFragment.md) can be viewed, removed, imported, and exported.

## Layout

![](https://i.imgur.com/XluMSz8.jpg)

## Snippet

```kt
class DataFragment : Fragment(), CellClickListener {
  private val TAG = "DATA"

  private var _binding: FragmentDataBinding? = null
  private val binding get() = _binding!!

  private lateinit var adapter: RecyclerViewAdapter

  private lateinit var db: DBHelper
  private lateinit var prefs: Prefs

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    _binding = FragmentDataBinding.inflate(inflater, container, false)
    val root: View = binding.root

    // Gets the instance of the prefs and dbhelper
    prefs = Prefs.getInstance(requireContext())!!
    db = DBHelper.getInstance(requireContext())!!

    // Gets the current session saved to the device
    val session = prefs.getSession()
    db.printData(session) // DEBUG

    // Creates an empty list of RowData
    val data = arrayListOf<RowData>()
    if (session > 0L) {
      // Loads the RowData for that session from the database
      data.addAll(db.getData(session))
    }

    // Sets up the display
    adapter = RecyclerViewAdapter(data, this)

    binding.rvDatapoints.adapter = adapter
    binding.rvDatapoints.layoutManager = LinearLayoutManager(requireContext())
    ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvDatapoints)

    // Sets up what happens when the Export CSV button is clicked
    binding.btnExport.setOnClickListener {
      // Creates a variable to store the csv with the first value being label
      var csv = "label"
      // Adds the names of the routers as to the csv
      for (router in db.getSessionRouters(prefs.getSession())) {
        csv += "," + router.getBSSID() + "-" + router.getName()
      }
      // Creates a new line after going through the names of the routers
      csv += "\n"

      // Adds the label and accompanying RSSI values to the csv
      for (row in adapter.data) {
        csv += "${row.label},${row.values.joinToString(",")}\n"
      }

      // Saves the csv file
      write(csv, requireContext())
    }

    return root
  }

  private fun write(data: String, context: Context) {
    try {
      // Creates a file name based on when the data has been exported
      val name = "${LocalDate.now()}T${LocalTime.now().toString().replace(":", "-").replace(".", "-")}"
      // Saves the file to the device with the filename given
      val file =
        File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$name.csv")

      // Creates the file and prevents it from being written over
      val stream = FileOutputStream(file, false)
      stream.write(data.toByteArray())
      stream.flush()
      stream.close()

      // Notification that the file has been saved
      Toast.makeText(context,
        "Saved to ${file.path}",
        Toast.LENGTH_LONG).show()



    } catch (e: Exception) {
      Log.e(TAG, e.toString())
    } finally {

    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }

  private val itemTouchHelperCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
    override fun onMove(
      recyclerView: RecyclerView,
      viewHolder: RecyclerView.ViewHolder,
      target: RecyclerView.ViewHolder
    ): Boolean {
      return false
    }

    // Deletes the data when the entry is swiped
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
      val pos = viewHolder.adapterPosition
      db.deleteRow(adapter.getRecordId(pos))
      adapter.removeItem(pos)
    }
  }

  // Allows the entry of the data to be edited
  @SuppressLint("NotifyDataSetChanged")
  override fun onCellClickListener(data: RowData) {
    // Create a dialog box
    val dialog = DataFormDialog(data)
    dialog.acceptListener = {
      val values = dialog.getValues()
      val updated = db.updateRow(prefs.getSession(), data.recordId, values)
      if (updated) {
        data.values.clear()
        data.values.addAll(values)

        binding.rvDatapoints.adapter?.notifyDataSetChanged()
      }
    }

    dialog.cancelListener = {}

    dialog.show(parentFragmentManager, "Edit Data")
  }
}

```