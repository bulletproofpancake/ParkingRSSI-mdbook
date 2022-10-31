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

    prefs = Prefs.getInstance(requireContext())!!
    db = DBHelper.getInstance(requireContext())!!

    val session = prefs.getSession()
    db.printData(session) // DEBUG

    val data = arrayListOf<RowData>()
    if (session > 0L) {
      data.addAll(db.getData(session))
    }

    adapter = RecyclerViewAdapter(data, this)

    binding.rvDatapoints.adapter = adapter
    binding.rvDatapoints.layoutManager = LinearLayoutManager(requireContext())
    ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.rvDatapoints)

    binding.btnExport.setOnClickListener {
      var csv = "label"
      for (router in db.getSessionRouters(prefs.getSession())) {
        csv += "," + router.getBSSID() + "-" + router.getName()
      }
      csv += "\n"

      for (row in adapter.data) {
        csv += "${row.label},${row.values.joinToString(",")}\n"
      }

      write(csv, requireContext())
    }

    return root
  }

  private fun write(data: String, context: Context) {
    try {
      val name = "${LocalDate.now()}T${LocalTime.now().toString().replace(":", "-").replace(".", "-")}"
      val file =
        File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$name.csv")

      val stream = FileOutputStream(file, false)
      stream.write(data.toByteArray())
      stream.flush()
      stream.close()

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

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
      val pos = viewHolder.adapterPosition
      db.deleteRow(adapter.getRecordId(pos))
      adapter.removeItem(pos)
    }
  }

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